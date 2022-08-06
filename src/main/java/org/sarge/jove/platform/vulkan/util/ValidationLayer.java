package org.sarge.jove.platform.vulkan.util;

import static java.util.stream.Collectors.toCollection;

import java.util.*;

import org.sarge.jove.platform.vulkan.VkLayerProperties;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.VulkanFunction.StructureVulkanFunction;
import org.sarge.lib.util.Check;

import com.sun.jna.ptr.IntByReference;

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
	 * Enumerates validation layers supported by the platform or a physical device.
	 * <p>
	 * Note that validation layers at the device level are deprecated.
	 * <p>
	 * The {@link Set#contains(Object)} method considers a layer to be a member if a matching entry with an equal or greater version number is present.
	 * <p>
	 * @param count			Number of layers
	 * @param func			Layers function
	 * @return Validation layers
	 */
	public static Set<ValidationLayer> layers(IntByReference count, StructureVulkanFunction<VkLayerProperties> func) {
		return Arrays
				.stream(func.invoke(count, VkLayerProperties::new))
				.map(ValidationLayer::of)
				.collect(toCollection(ValidationLayerSet::new));
	}

	/**
	 * Enumerates validation layers supported by this platform.
	 * @param lib 			Vulkan library
	 * @param count			Number of layers
	 * @return Validation layers supported by this platform
	 */
	public static Set<ValidationLayer> layers(VulkanLibrary lib, IntByReference count) {
		final StructureVulkanFunction<VkLayerProperties> func = (c, array) -> lib.vkEnumerateInstanceLayerProperties(c, array);
		return layers(count, func);
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
	}

	/**
	 * Convenience constructor for a validation layer with a version number of <b>one</b>.
	 * @param name Layer name
	 */
	public ValidationLayer(String name) {
		this(name, 1);
	}
}
