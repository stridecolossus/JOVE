package org.sarge.jove.platform.vulkan.memory;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.*;

import java.util.*;
import java.util.function.Predicate;

import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.util.EnumMask;

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
	 * @param device 		Logical device
	 * @param types			Memory types
	 * @return Allocator
	 * @see MemoryType#enumerate(VkPhysicalDeviceMemoryProperties)
	 * @see LogicalDevice#limits()
	 */
	public static Allocator create(LogicalDevice device, MemoryType[] types) {
//		final int max = limits.maxMemoryAllocationCount;
//    	final long page = limits.bufferImageGranularity;
    	return new Allocator(device, types);
	}

	private final LogicalDevice device;
	private final MemoryType[] types;
	private long page = 1024;				// TODO
	private int max = Integer.MAX_VALUE;
	private int count;

	/**
	 * Constructor.
	 * @param dev			Logical device
	 * @param types 		Memory types
	 * @param max			Maximum number of allocations
	 * @param page	Memory page granularity
	 */
	public Allocator(LogicalDevice dev, MemoryType[] types) {
		this.device = requireNonNull(dev);
		this.types = Arrays.copyOf(types, types.length);
//		this.max = requireOneOrMore(max);
//		this.granularity = requireOneOrMore(granularity);
	}

//	/**
//	 * Copy constructor.
//	 */
//	protected Allocator(Allocator allocator) {
//		this(allocator.device, allocator.types, allocator.max, allocator.granularity);
//	}

	/**
	 * @return Number of allocations
	 */
	public final int count() {
		return count;
	}

	public void setPageSize(long page) {
		this.page = requireOneOrMore(page);
	}

	public void setMaximumAllocationCount(int max) {
		this.max = requireZeroOrMore(max);
	}

//	/**
//	 * @return Maximum number of allocations supported by the hardware
//	 */
//	public final int max() {
//		return max;
//	}
//
//	/**
//	 * @return Page size granularity
//	 */
//	public final long granularity() {
//		return granularity;
//	}

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
	 * @param requirements			Requirements
	 * @param properties			Memory properties
	 * @return Selected memory type
	 * @throws AllocationException if no memory type matches the request
	 */
	private MemoryType select(VkMemoryRequirements requirements, MemoryProperties<?> properties) throws AllocationException {
		final var matcher = new MemoryTypeMatcher(properties);
		return EnumMask.stream(requirements.memoryTypeBits)
				.mapToObj(n -> types[n])
				.filter(matcher)
				.findAny()
				.or(matcher::fallback)
				.orElseThrow(() -> new AllocationException("No available memory type: requirements=%s properties=%s".formatted(requirements, properties)));
	}

	/**
	 * Matches a memory type for the given properties and records the fallback as a side-effect.
	 */
	private static class MemoryTypeMatcher implements Predicate<MemoryType> {
		private final MemoryProperties<?> properties;
		private MemoryType fallback;

		public MemoryTypeMatcher(MemoryProperties<?> properties) {
			this.properties = properties;
		}

		@Override
		public boolean test(MemoryType type) {
			// Skip if this type does not match the minimal requirements
			if(!matches(type, properties.required())) {
				return false;
			}

			// Check for optimal match
			if(matches(type, properties.optimal())) {
				return true;
			}

			// Record fallback candidate
			if(fallback == null) {
				fallback = type;
			}

			return false;
		}

		private static boolean matches(MemoryType type, Set<VkMemoryProperty> properties) {
			return type.properties().containsAll(properties);
		}

		private Optional<MemoryType> fallback() {
			return Optional.ofNullable(fallback);
		}
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
		if(count >= max) throw new AllocationException("Number of allocations exceeds the hardware limit");

		// Quantise the requested size
		final long pages = pages(size);
		assert pages > 0;

		// Init memory descriptor
		final var info = new VkMemoryAllocateInfo();
		info.allocationSize = page * pages;
		info.memoryTypeIndex = type.index();

		// Allocate memory
		final MemoryLibrary vulkan = device.library();
		final Pointer pointer = new Pointer();
		try {
			vulkan.vkAllocateMemory(device, info, null, pointer);
		}
		catch(VulkanException e) {
			throw new AllocationException("Cannot allocate device memory: type=%s size=%d".formatted(type, size));
		}

		// Create device memory
		++count;
		return new DefaultDeviceMemory(pointer.get(), device, type, size);
	}

	/**
	 * Quantises the requested memory size to the configured page granularity.
	 * @param size Memory size (bytes)
	 * @return Number of pages
	 */
	protected long pages(long size) {
		return 1 + (size - 1) / page;
	}

	/**
	 * Clears the allocation count.
	 */
	protected final void reset() {
		count = 0;
	}
}
