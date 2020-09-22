package org.sarge.jove.platform.vulkan;

import java.util.Set;

import org.sarge.lib.util.Check;

import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

/**
 * Descriptor for a <i>validation layer</i>.
 * @author Sarge
 */
public record ValidationLayer(String name, int version) {
	/**
	 * Standard validation layer.
	 */
	public static final ValidationLayer STANDARD_VALIDATION = new ValidationLayer("VK_LAYER_LUNARG_standard_validation");

	/**
	 * Helper used to retrieve supported validation layers.
	 */
	public static final Support<VkLayerProperties, ValidationLayer> SUPPORTED_LAYERS = new Support<>() {
		@Override
		public Set<ValidationLayer> enumerate(VulkanFunction<VkLayerProperties> func) {
			return enumerate(func, new IntByReference(), new VkLayerProperties());
		}

		@Override
		protected ValidationLayer map(VkLayerProperties layer) {
			return new ValidationLayer(Native.toString(layer.layerName), layer.implementationVersion);
		}
	};

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
}
