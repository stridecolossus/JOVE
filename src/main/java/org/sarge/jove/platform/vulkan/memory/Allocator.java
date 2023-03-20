package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.*;

import java.util.*;
import java.util.function.Predicate;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.util.BitField;

import com.sun.jna.ptr.PointerByReference;

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
		final DeviceLimits limits = dev.limits();
		final int max = limits.value("maxMemoryAllocationCount");
    	final long page = limits.value("bufferImageGranularity");

    	// Create allocator
    	return new Allocator(dev, types, max, page);
	}

	private final DeviceContext dev;
	private final MemoryType[] types;
	private final long page;
	private final int max;
	private int count;

	/**
	 * Constructor.
	 * @param dev		Logical device
	 * @param types 	Memory types
	 * @param max		Maximum number of allocations
	 * @param page		Memory page granularity
	 */
	public Allocator(DeviceContext dev, MemoryType[] types, int max, long page) {
		this.dev = notNull(dev);
		this.types = Arrays.copyOf(types, types.length);
		this.max = oneOrMore(max);
		this.page = oneOrMore(page);
	}

	/**
	 * Copy constructor.
	 */
	protected Allocator(Allocator allocator) {
		this(allocator.dev, allocator.types, allocator.max, allocator.page);
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
	public final long page() {
		return page;
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
	 * The requested memory size is quantised to the optimal {@link #page()} size granularity specified by the hardware.
	 * Additionally the size of the allocated memory may be larger due to alignment constraints.
	 * i.e. The actual allocation may be larger than {@link #size} however in either case this is transparent to the resultant device memory instance.
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
		final long actual = quantise(size);

		// Init memory descriptor
		final var info = new VkMemoryAllocateInfo();
		info.allocationSize = oneOrMore(actual);
		info.memoryTypeIndex = type.index();

		// Allocate memory
		final VulkanLibrary lib = dev.library();
		final PointerByReference ref = dev.factory().pointer();
		final int result = lib.vkAllocateMemory(dev, info, null, ref);

		// Check allocated
		if(result != VulkanLibrary.SUCCESS) {
			throw new AllocationException("Cannot allocate memory: type=%s size=%d error=%d".formatted(type, size, result));
		}

		// Create device memory
		++count;
		return new DefaultDeviceMemory(new Handle(ref), dev, type, size);
	}

	/**
	 * Quantises the requested memory size to the configured page size.
	 * @param size Memory size (bytes)
	 * @return Quantised size
	 */
	protected long quantise(long size) {
		return (1 + (size / page)) * page;
	}

	/**
	 * Clears the allocation count.
	 */
	protected final void reset() {
		count = 0;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("types", types.length)
				.append("allocations", String.format("%d/%d", count, max))
				.append("page", page)
				.build();
	}
}
