package org.sarge.jove.platform.vulkan.common;

import static java.util.stream.Collectors.toCollection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.sarge.jove.platform.vulkan.VkLayerProperties;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;
import org.sarge.lib.util.Check;

/**
 * A <i>validation layer</i> specifies a Vulkan diagnostics or interceptor layer.
 * @author Sarge
 */
public record ValidationLayer(String name, int version) {
	/**
	 * Standard validation layer.
	 */
	public static final ValidationLayer STANDARD_VALIDATION = new ValidationLayer("VK_LAYER_KHRONOS_validation");

	/**
	 * Creates a validation layer from the given descriptor.
	 * @param layer Descriptor
	 * @return New validation layer
	 */
	public static ValidationLayer of(VkLayerProperties layer) {
		return new ValidationLayer(new String(layer.layerName), layer.implementationVersion);
	}

	/**
	 * Enumerates validation layers.
	 * <p>
	 * The {@link Set#contains(Object)} method considers a layer to be a member if a matching entry with an equal or greater version number is present.
	 * <p>
	 * @param lib			Vulkan
	 * @param func			Layers function
	 * @return Validation layers
	 */
	public static Set<ValidationLayer> enumerate(VulkanLibrary lib, VulkanFunction<VkLayerProperties> func) {
		return Arrays
				.stream(VulkanFunction.enumerate(func, lib, VkLayerProperties::new))
				.map(ValidationLayer::of)
				.collect(toCollection(ValidationLayerSet::new));
	}

	/**
	 * Set of validation layers with version number membership.
	 */
	static class ValidationLayerSet extends HashSet<ValidationLayer> {
		@Override
		public boolean contains(Object obj) {
			// Check matching layer
			if(super.contains(obj)) {
				return true;
			}

			// Check for layer with higher version number
			if(obj instanceof ValidationLayer layer) {
				return
						stream()
						.filter(e -> e.name.equals(layer.name))
						.anyMatch(e -> layer.version <= e.version);
			}

			// Layer not present
			return false;
		}
	}

	/**
	 * Constructor.
	 * @param name				Layer name
	 * @param version			Version number
	 */
	public ValidationLayer {
		Check.notEmpty(name);
		Check.oneOrMore(version);
	}

	/**
	 * Convenience constructor for a validation layer with a version number of <b>one</b>.
	 * @param name Layer name
	 */
	public ValidationLayer(String name) {
		this(name, 1);
	}
}
