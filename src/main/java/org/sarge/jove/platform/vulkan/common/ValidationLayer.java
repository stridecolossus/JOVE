package org.sarge.jove.platform.vulkan.common;

import java.util.Set;

import org.sarge.jove.platform.vulkan.VkLayerProperties;
import org.sarge.jove.platform.vulkan.util.Support;
import org.sarge.jove.util.Check;

import com.sun.jna.Native;

/**
 * Descriptor for a <i>validation layer</i>.
 * @author Sarge
 */
public record ValidationLayer(String name, int version) {
	/**
	 * Standard validation layer.
	 */
	public static final ValidationLayer STANDARD_VALIDATION = new ValidationLayer("VK_LAYER_LUNARG_standard_validation"); // TODO - VK_LAYER_KHRONOS_validation v1.1.1114.0

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

	/**
	 * TODO
	 * @param available Available layers
	 * @return Whether this layer is present in the given set of available validation layers
	 */
	public boolean isPresent(Set<ValidationLayer> available) {
		// Check for exact match
		if(available.contains(this)) {
			return true;
		}

		// Otherwise find matching layer with higher version number
		return available
				.stream()
				.filter(layer -> layer.name.equals(this.name))
				.anyMatch(layer -> layer.version <= this.version);
	}

	/**
	 * Support helper for retrieval of supporting validation layers.
	 */
	public static class ValidationLayerSupport extends Support<VkLayerProperties, ValidationLayer> {
		@Override
		protected VkLayerProperties identity() {
			return new VkLayerProperties();
		}

		@Override
		protected ValidationLayer map(VkLayerProperties struct) {
			return new ValidationLayer(Native.toString(struct.layerName), struct.implementationVersion);
		}
	}
}
