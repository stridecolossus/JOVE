package org.sarge.jove.platform.vulkan.util;

import static org.sarge.lib.Validation.requireNotEmpty;

import java.util.HashSet;

import org.sarge.jove.platform.vulkan.VkLayerProperties;

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
	 * Constructor.
	 * @param name				Layer name
	 * @param version			Version number
	 */
	public ValidationLayer {
		requireNotEmpty(name);
	}

	/**
	 * Convenience constructor for a validation layer with a version number of <b>one</b>.
	 * @param name Layer name
	 */
	public ValidationLayer(String name) {
		this(name, 1);
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
}
