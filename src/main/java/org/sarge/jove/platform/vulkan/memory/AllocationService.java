package org.sarge.jove.platform.vulkan.memory;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.sarge.jove.platform.vulkan.VkMemoryProperty;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceMemoryProperties;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;
import org.sarge.jove.platform.vulkan.memory.Allocator.PageAllocator;
import org.sarge.jove.util.MathsUtil;

/**
 * The <i>allocation service</i> encapsulates the process of selecting and allocating the optimal {@link MemoryType} for a given memory request.
 * <p>
 * Usage:
 * <pre>
 * 	// Create memory allocator
 * 	Allocator allocator = ...
 *
 * 	// Create service
 * 	AllocationService service = AllocationService.create(dev, allocator);
 *
 * 	// Retrieve memory requirements
 * 	VkMemoryRequirements reqs = ...
 *
 * 	// Configure memory properties
 * 	MemoryProperties<?> props = new MemoryProperties.Builder()
 * 		.required(VkMemoryProperty.HOST_VISIBLE)
 * 		.optimal(VkMemoryProperty.HOST_CACHED)
 * 		.usage(VkBufferUsage.VERTEX_BUFFER)
 * 		.build();
 *
 * 	// Allocate memory
 * 	DeviceMemory mem = service.allocate(reqs, props);
 * </pre>
 * <p>
 * @see Allocator
 * @see MemoryProperties
 * @see <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/VkPhysicalDeviceMemoryProperties.html">Vulkan documentation</a>
 * @author Sarge
 */
public class AllocationService {
	/**
	 * Creates the allocator service.
	 * @param dev			Logical device
	 * @param allocator		Memory allocator
	 * @return New allocation service
	 */
	public static AllocationService create(LogicalDevice dev, Allocator allocator) {
		// Retrieve supported memory types
		final var props = new VkPhysicalDeviceMemoryProperties();
		final VulkanLibrary lib = dev.library();
		lib.vkGetPhysicalDeviceMemoryProperties(dev.parent(), props);

		// Enumerate memory types
		final List<MemoryType> types = MemoryType.enumerate(props);

		// Create service
		return new AllocationService(allocator, types);
	}

	/**
	 * Helper - Creates and configures an allocation service implemented by a memory pool.
	 * <p>
	 * The memory allocator is configured as follows:
	 * <ul>
	 * <li>Device memory is served from a {@link PoolAllocator} limited to {@link VkPhysicalDeviceLimits#maxMemoryAllocationCount}</li>
	 * <li>Memory blocks are allocated by a {@link PageAllocator} sized by {@link VkPhysicalDeviceLimits#bufferImageGranularity}</li>
	 * </ul>
	 * <p>
	 * @param dev Logical device
	 * @return New default allocation service
	 */
	public static AllocationService pool(LogicalDevice dev) {
		final VkPhysicalDeviceLimits limits = dev.parent().properties().limits();
		final Allocator allocator = Allocator.allocator(dev);
		final Allocator paged = new PageAllocator(allocator, limits.bufferImageGranularity);
		final Allocator pool = new PoolAllocator(paged, limits.maxMemoryAllocationCount);
		return create(dev, pool);
	}

	private final Allocator allocator;
	private final List<MemoryType> types;

	/**
	 * Constructor.
	 * @param allocator		Memory allocator
	 * @param types			Memory types
	 */
	AllocationService(Allocator allocator, List<MemoryType> types) {
		this.allocator = notNull(allocator);
		this.types = List.copyOf(types);
	}

	/**
	 * Selects and allocates memory.
	 * @param reqs			Memory requirements
	 * @param props			Memory properties
	 * @return New device memory
	 * @throws AllocationException if the memory cannot be allocated
	 * @see #allocate(MemoryType, long)
	 */
	public DeviceMemory allocate(VkMemoryRequirements reqs, MemoryProperties<?> props) throws AllocationException {
		// Filter available memory types
		final var candidates = types
				.stream()
				.filter(type -> MathsUtil.isBit(reqs.memoryTypeBits, type.index()))
				.collect(toList());

		// Find matching memory type
		final MemoryType type =
				find(candidates, props.optimal())
				.or(() -> find(candidates, props.required()))
				.orElseThrow(() -> new AllocationException(String.format("No available memory type: requirements=%s properties=%s", reqs, props)));

		// Delegate to allocator
		return allocator.allocate(type, reqs.size);
	}

	/**
	 * Finds a memory type with the given properties.
	 */
	private static Optional<MemoryType> find(List<MemoryType> types, Set<VkMemoryProperty> props) {
		if(props.isEmpty()) {
			return Optional.empty();
		}

		return types
				.stream()
				.filter(type -> type.properties().containsAll(props))
				.findAny();
	}

	/**
	 * Closes this service and releases allocated memory.
	 */
	public void close() {
		if(allocator instanceof PoolAllocator pool) {
			pool.close();
		}
	}
}
