package org.sarge.jove.platform.vulkan.memory;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireOneOrMore;

import org.sarge.jove.common.AbstractTransientObject;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;

/**
 * An <i>allocator</i> is responsible for allocating device memory for a given request.
 * @author Sarge
 */
public class Allocator extends AbstractTransientObject {
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
	}

	/**
	 * Creates an allocator for the given device.
	 * <p>
	 * The allocator is configured by the following device limits:
	 * <ul>
	 * <li>{@code bufferImageGranularity} configures the granularity of allocated memory pages</li>
	 * <li>{@Code maxMemoryAllocationCount} caps the number of allowed allocations</li>
	 * </ul>
	 * <p>
	 * @param device	Logical device
	 * @param types		Memory types
	 * @return Allocator
	 */
	public static Allocator of(LogicalDevice device, MemoryType[] types) {
		final var limits = device.limits();
		final long page = limits.get("bufferImageGranularity");
		final int max = limits.get("maxMemoryAllocationCount");
		return new Allocator(device, new MemorySelector(types), page, max);
	}

	private final LogicalDevice device;
	private final MemorySelector selector;
	private final long page;
	private final int max;
	private int count;

	/**
	 * Constructor.
	 * @param device		Logical device
	 * @param selector		Memory selector
	 * @param page			Page granularity (bytes)
	 * @param max			Maximum number of allocations
	 */
	Allocator(LogicalDevice device, MemorySelector selector, long page, int max) {
		this.device = requireNonNull(device);
		this.selector = requireNonNull(selector);
		this.page = requireOneOrMore(page);
		this.max = max > 0 ? max : Integer.MAX_VALUE;
	}

	/**
	 * Copy constructor.
	 * @param that Delegate allocator
	 */
	protected Allocator(Allocator that) {
		this(that.device, that.selector, that.page, that.max);
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
	 * @param requirements			Memory requirements
	 * @param properties			Memory properties
	 * @return Allocated memory
	 * @throws AllocationException if there is no matching memory type for the request or the memory cannot be allocated by the hardware
	 */
	public DeviceMemory allocate(VkMemoryRequirements requirements, MemoryProperties<?> properties) throws AllocationException {
		final MemoryType type = selector
				.select(requirements.memoryTypeBits, properties)
				.orElseThrow(() -> new AllocationException("No available memory type: requirements=%s properties=%s".formatted(requirements, properties)));

		return allocate(type, requirements.size);
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
		requireOneOrMore(size);

		// Check maximum number of allocations
		if(count >= max) {
			throw new AllocationException("Number of allocations exceeds the hardware limit: " + max);
		}

		// Quantise the requested size
		final long pages = pages(size);
		assert pages > 0;

		// Init memory descriptor
		final var allocation = new VkMemoryAllocateInfo();
		allocation.sType = VkStructureType.MEMORY_ALLOCATE_INFO;
		allocation.allocationSize = page * pages;
		allocation.memoryTypeIndex = type.index();

		// Allocate memory
		final MemoryLibrary library = device.library();
		final Pointer pointer = new Pointer(size);
		try {
			library.vkAllocateMemory(device, allocation, null, pointer);
		}
		catch(VulkanException e) {
			throw new AllocationException("Cannot allocate device memory: type=%s size=%d result=%s".formatted(type, size, e.result()));
		}

		// Create device memory
		++count;
		return new DefaultDeviceMemory(pointer.handle(), device, type, size);
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
}
