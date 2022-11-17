package org.sarge.jove.platform.vulkan.memory;

import java.util.*;
import java.util.function.Predicate;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
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
	 * @param dev Physical device
	 * @return New memory selector
	 * @see MemoryType#enumerate(VkPhysicalDeviceMemoryProperties)
	 */
	public static MemorySelector create(PhysicalDevice dev) {
		// Retrieve supported memory types
		final var props = new VkPhysicalDeviceMemoryProperties();
		final VulkanLibrary lib = dev.instance().library();
		lib.vkGetPhysicalDeviceMemoryProperties(dev, props);

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
		/**
		 * Filter for the required memory properties that also records the fallback as a side-effect.
		 */
		class FallbackPredicate implements Predicate<MemoryType> {
			private MemoryType fallback;

			@Override
			public boolean test(MemoryType type) {
				if(type.matches(props.required())) {
    				if(type.matches(props.optimal())) {
    					// Found optimal match
    					return true;
    				}

    				// Record fallback candidate
					if(fallback == null) {
						fallback = type;
					}
				}
				return false;
			}

			private Optional<MemoryType> fallback() {
				return Optional.ofNullable(fallback);
			}
		}

		// Walk candidate memory types and match against the requested properties
		final FallbackPredicate predicate = new FallbackPredicate();
		return new Mask(reqs.memoryTypeBits)
				.stream()
				.mapToObj(n -> types[n - 1])
				.filter(predicate)
				.findAny()
				.or(predicate::fallback)
				.orElseThrow(() -> new AllocationException("No available memory type: requirements=%s properties=%s".formatted(reqs, props)));
	}
}
