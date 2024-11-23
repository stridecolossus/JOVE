package org.sarge.jove.platform.vulkan.memory;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireOneOrMore;

import java.util.*;
import java.util.function.Predicate;

import org.sarge.jove.foreign.PointerReference;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.util.BitField;

/**
 * An <i>allocator</i> is responsible for allocating device memory for a given request.
 * @author Sarge
 */
public class Allocator {
	/**
	 * An <i>allocation exception</i> is thrown when this allocator cannot allocate memory.
	 */
	public static class AllocationException extends RuntimeException {
		/**
		 * Constructor.
		 * @param message Message
		 */
		public AllocationException(String message) {
			super(message);
		}

		/**
		 * Constructor.
		 * @param cause Cause
		 */
		protected AllocationException(Throwable cause) {
			super(cause);
		}
	}

	/**
	 * Creates and configures a memory allocator for the given device.
	 * @param dev Logical device
	 * @return Allocator
	 * @see MemoryType#enumerate(VkPhysicalDeviceMemoryProperties)
	 * @see LogicalDevice#limits()
	 */
	public static Allocator create(LogicalDevice dev) {
		// Retrieve supported memory types
		final var props = dev.parent().memory();
		final MemoryType[] types = MemoryType.enumerate(props);

		// Lookup hardware limits
		final VkPhysicalDeviceLimits limits = dev.limits();
		final int max = limits.maxMemoryAllocationCount;
    	final long page = limits.bufferImageGranularity;

    	// Create allocator
    	return new Allocator(dev, types, max, page);
	}

	private final DeviceContext dev;
	private final MemoryType[] types;
	private final long granularity;
	private final int max;
	private int count;

	/**
	 * Constructor.
	 * @param dev			Logical device
	 * @param types 		Memory types
	 * @param max			Maximum number of allocations
	 * @param granularity	Memory page granularity
	 */
	public Allocator(DeviceContext dev, MemoryType[] types, int max, long granularity) {
		this.dev = requireNonNull(dev);
		this.types = Arrays.copyOf(types, types.length);
		this.max = requireOneOrMore(max);
		this.granularity = requireOneOrMore(granularity);
	}

	/**
	 * Copy constructor.
	 */
	protected Allocator(Allocator allocator) {
		this(allocator.dev, allocator.types, allocator.max, allocator.granularity);
	}

	/**
	 * @return Number of allocations
	 */
	public final int count() {
		return count;
	}

	/**
	 * @return Maximum number of allocations supported by the hardware
	 */
	public final int max() {
		return max;
	}

	/**
	 * @return Page size granularity
	 */
	public final long granularity() {
		return granularity;
	}

	/**
	 * Allocates device memory for the given request.
	 * <p>
	 * The {@link <a href="https://registry.khronos.org/vulkan/specs/1.3-extensions/man/html/VkPhysicalDeviceMemoryProperties.html">Vulkan documentation</a>}
	 * describes the suggested approach to select the appropriate memory type for a given request as follows:
	 * <ol>
	 * <li>Filter the candidate memory types by the {@link VkMemoryRequirements#memoryTypeBits} mask</li>
	 * <li>Find the <i>optimal</i> type matching the given memory properties</li>
	 * <li>Otherwise fallback to the <b>first</b> available type matching the minimal <i>required</i> properties</li>
	 * </ol>
	 * <p>
	 * @param reqs			Memory requirements
	 * @param props			Memory properties
	 * @return Allocated memory
	 * @throws IllegalAccessException if {@link #size} is not positive
	 * @throws AllocationException if there is no matching memory type for the request or the memory cannot be allocated by the hardware
	 */
	public DeviceMemory allocate(VkMemoryRequirements reqs, MemoryProperties<?> props) throws AllocationException {
		final MemoryType type = select(reqs, props);
		return allocate(type, reqs.size);
	}

	/**
	 * Selects the memory type for the given request.
	 * @param reqs			Requirements
	 * @param props			Memory properties
	 * @return Selected memory type
	 * @throws AllocationException if no memory type matches the request
	 */
	private MemoryType select(VkMemoryRequirements reqs, MemoryProperties<?> props) throws AllocationException {
		/**
		 * Matches a memory type for the given properties and records the fallback as a side-effect.
		 */
		class FallbackMatcher implements Predicate<MemoryType> {
			private MemoryType fallback;

			@Override
			public boolean test(MemoryType type) {
				// Skip if this type does not match the minimal requirements
				if(!type.matches(props.required())) {
					return false;
				}

				// Check for optimal match
    			if(type.matches(props.optimal())) {
    				return true;
    			}

    			// Record fallback candidate
				if(fallback == null) {
					fallback = type;
				}

				return false;
			}

			private Optional<MemoryType> fallback() {
				return Optional.ofNullable(fallback);
			}
		}

		// Walk candidate memory types and match against the requested properties
		final var matcher = new FallbackMatcher();
		return new BitField(reqs.memoryTypeBits)
				.stream()
				.mapToObj(n -> types[n])
				.filter(matcher)
				.findAny()
				.or(matcher::fallback)
				.orElseThrow(() -> new AllocationException("No available memory type: requirements=%s properties=%s".formatted(reqs, props)));
	}

	/**
	 * Allocates memory of the given type.
	 * <p>
	 * The requested memory size is quantised to the optimal page size {@link #granularity()} specified by the hardware.
	 * Additionally the size of the allocated memory may be larger due to alignment constraints.
	 * In either case this is transparent to the resultant device memory instance.
	 * <p>
	 * @param type		Memory type
	 * @param size		Size (bytes)
	 * @return Allocated memory
	 * @throws IllegalAccessException if {@link #size} is not positive
	 * @throws AllocationException if the memory cannot be allocated
	 */
	protected DeviceMemory allocate(MemoryType type, long size) throws AllocationException {
		// Check maximum number of allocations
		if(count >= max) throw new AllocationException("Number of allocations exceeds the hardware limit".formatted(count, max));

		// Quantise the requested size
		final long pages = pages(size);
		assert pages > 0;

		// Init memory descriptor
		final var info = new VkMemoryAllocateInfo();
		info.allocationSize = granularity * pages;
		info.memoryTypeIndex = type.index();

		// Allocate memory
		final Vulkan vulkan = dev.vulkan();
		final PointerReference ref = vulkan.factory().pointer();
		// final int result =
		// TODO - why specific check?
		vulkan.library().vkAllocateMemory(dev, info, null, ref);

//		// Check allocated
//		if(result != VulkanLibrary.SUCCESS) {
//			throw new AllocationException("Cannot allocate memory: type=%s size=%d error=%d".formatted(type, size, result));
//		}

		// Create device memory
		++count;
		return new DefaultDeviceMemory(ref.handle(), dev, type, size);
	}

	/**
	 * Quantises the requested memory size to the configured page granularity.
	 * @param size Memory size (bytes)
	 * @return Number of pages
	 */
	protected long pages(long size) {
		return 1 + (size - 1) / granularity;
	}

	/**
	 * Clears the allocation count.
	 */
	protected final void reset() {
		count = 0;
	}
}
