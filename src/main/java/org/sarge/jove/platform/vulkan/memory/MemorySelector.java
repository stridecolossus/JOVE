package org.sarge.jove.platform.vulkan.memory;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.sarge.jove.platform.vulkan.VkMemoryProperty;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceMemoryProperties;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;
import org.sarge.jove.util.Mask;

/**
 * The <i>memory selector</i> implements the recommended algorithm for selecting a {@link MemoryType} for a given allocation request.
 * @see VkMemoryRequirements
 * @see MemoryProperties
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
		final MemoryType[] types = MemoryType.enumerate(props);

		// Create selector
		return new MemorySelector(types);
	}

	private final MemoryType[] types;

	/**
	 * Constructor.
	 * @param types Memory types
	 */
	public MemorySelector(MemoryType[] types) {
		this.types = Arrays.copyOf(types, types.length);
	}

	/**
	 * Selects the memory type for the given request.
	 * @param reqs			Requirements
	 * @param props			Memory properties
	 * @return Selected memory type
	 * @throws AllocationException if no memory type matches the request
	 */
	public MemoryType select(VkMemoryRequirements reqs, MemoryProperties<?> props) throws AllocationException {
		// Filter available memory types
		final Mask mask = new Mask(reqs.memoryTypeBits);
		final List<MemoryType> candidates = IntStream
				.range(0, types.length)
				.filter(mask::bit)
				.mapToObj(n -> types[n])
				.collect(toList());

		// Find matching memory type
		return
				find(candidates, props.optimal())
				.or(() -> find(candidates, props.required()))
				.orElseThrow(() -> new AllocationException(String.format("No available memory type: requirements=%s properties=%s", reqs, props)));
	}

	/**
	 * Finds a memory type matching the given properties.
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
