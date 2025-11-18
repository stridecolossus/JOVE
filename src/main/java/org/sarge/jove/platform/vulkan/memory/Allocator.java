package org.sarge.jove.platform.vulkan.memory;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireOneOrMore;

import java.util.*;
import java.util.function.Predicate;

import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanException;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.util.EnumMask;

/**
 * An <i>allocator</i> is responsible for allocating device memory for a given request.
 * @author Sarge
 */
public class Allocator {
	/**
	 * An <i>allocation exception</i> is thrown when this allocator fails to allocate memory.
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

	private final LogicalDevice device;
	private final MemoryType[] types;
	private final long page;
	private final int max;
	private int count;

	/**
	 * Constructor.
	 * @param device		Logical device
	 * @param types 		Memory types
	 */
	public Allocator(LogicalDevice device, MemoryType[] types) {
		final var limits = device.limits();
		this.device = requireNonNull(device);
		this.types = Arrays.copyOf(types, types.length);
		this.page = requireOneOrMore((long) limits.get("bufferImageGranularity"));
		this.max = constrain(limits.get("maxMemoryAllocationCount"), Integer.MAX_VALUE);
	}

	private static int constrain(int value, int def) {
		if(value == -1) {
			return def;
		}
		else {
			return value;
		}
	}

	/**
	 * @return Logical device
	 */
	public LogicalDevice device() {
		return device;
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
	 * @return Page size
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
	 * @param requirements			Memory requirements
	 * @param properties			Memory properties
	 * @return Allocated memory
	 * @throws AllocationException if there is no matching memory type for the request or the memory cannot be allocated by the hardware
	 */
	public DeviceMemory allocate(VkMemoryRequirements requirements, MemoryProperties<?> properties) throws AllocationException {
		final MemoryType type = select(requirements, properties);
		return allocate(type, requirements.size);
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
	 * The requested memory size is quantised to the optimal page size {@link #page()} specified by the hardware.
	 * Additionally the size of the allocated memory may be larger due to alignment constraints.
	 * <p>
	 * @param type		Memory type
	 * @param size		Size (bytes)
	 * @return Allocated memory
	 * @throws IllegalAccessException if {@link #size} is not positive
	 * @throws AllocationException if the memory cannot be allocated
	 */
	protected DeviceMemory allocate(MemoryType type, long size) throws AllocationException {
		// Check maximum number of allocations
		if(count >= max) {
			throw new AllocationException("Number of allocations exceeds the hardware limit: " + max);
		}

		// Quantise the requested size
		final long pages = pages(size);
		assert pages > 0;

		// Init memory descriptor
		final var allocation = new VkMemoryAllocateInfo();
		allocation.allocationSize = page * pages;
		allocation.memoryTypeIndex = type.index();

		// Allocate memory
		final MemoryLibrary library = device.library();
		final Pointer pointer = new Pointer();
		try {
			library.vkAllocateMemory(device, allocation, null, pointer);
		}
		catch(VulkanException e) {
			throw new AllocationException("Cannot allocate device memory: type=%s size=%d".formatted(type, size));
		}

		// Create device memory
		++count;
		return new DefaultDeviceMemory(pointer.get(), device, type, size);
	}

	/**
	 * Quantises the requested memory size to the configured page size.
	 * @param size Memory size (bytes)
	 * @return Number of pages
	 */
	protected long pages(long size) {
		return 1 + (size - 1) / page;
	}
	// TODO - this feels a bit 'contrived', can it be made simpler and more expressive?

	/**
	 * Clears the allocation count.
	 */
	protected final void reset() {
		count = 0;
	}
}
