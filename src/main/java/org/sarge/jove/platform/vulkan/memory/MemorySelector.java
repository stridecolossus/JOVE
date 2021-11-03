package org.sarge.jove.platform.vulkan.memory;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.sarge.jove.platform.vulkan.VkMemoryProperty;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceMemoryProperties;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;
import org.sarge.jove.util.MathsUtil;

/**
 *
 * <p>
 * @see <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/VkPhysicalDeviceMemoryProperties.html">Vulkan documentation</a>
 * @author Sarge
 */
public class MemorySelector {
	/**
	 * Creates a memory selector for the given device.
	 * @param dev Logical device
	 * @return New memory selector
	 */
	public static MemorySelector create(LogicalDevice dev) {
		// Retrieve supported memory types
		final var props = new VkPhysicalDeviceMemoryProperties();
		final VulkanLibrary lib = dev.library();
		lib.vkGetPhysicalDeviceMemoryProperties(dev.parent(), props);

		// Enumerate memory types
		final List<MemoryType> types = MemoryType.enumerate(props);

		// Create selector
		return new MemorySelector(types);
	}

	private final List<MemoryType> types;

	/**
	 * Constructor.
	 * @param types Memory types
	 */
	public MemorySelector(List<MemoryType> types) {
		this.types = List.copyOf(types);
	}

	/**
	 * Selects the memory type for the given request.
	 * @param reqs			Requirements
	 * @param props			Memory properties
	 * @return
	 * @throws AllocationException if no memory type matches the request
	 */
	public MemoryType select(VkMemoryRequirements reqs, MemoryProperties<?> props) throws AllocationException {
		// Filter available memory types
		final var candidates = types
				.stream()
				.filter(type -> MathsUtil.isBit(reqs.memoryTypeBits, type.index()))
				.collect(toList());

		// Find matching memory type
		return
				find(candidates, props.optimal())
				.or(() -> find(candidates, props.required()))
				.orElseThrow(() -> new AllocationException(String.format("No available memory type: requirements=%s properties=%s", reqs, props)));
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
}