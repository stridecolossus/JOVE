package org.sarge.jove.platform.vulkan.common;

import java.util.Set;

import org.sarge.jove.util.Check;

/**
 * Descriptor for a <i>validation layer</i>.
 * @author Sarge
 */
public record ValidationLayer(String name, int version) {
	/**
	 * Standard validation layer.
	 */
	public static final ValidationLayer STANDARD_VALIDATION = new ValidationLayer("VK_LAYER_KHRONOS_validation");

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
