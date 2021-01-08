package org.sarge.jove.platform.vulkan.common;

import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.VkLayerProperties;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;

import com.sun.jna.Native;

/**
 *
 * @author Sarge
 */
public class Supported {
	private final Set<String> extensions;
	private final Set<ValidationLayer> layers;

	/**
	 * Constructor.
	 * @param lib						Vulkan API
	 * @param extensionsFunction		Extensions function
	 * @param layersFunction			Validation layers function
	 */
	public Supported(VulkanLibrary lib, VulkanFunction<VkExtensionProperties> extensionsFunction, VulkanFunction<VkLayerProperties> layersFunction) {
		this.extensions = extensions(lib, extensionsFunction);
		this.layers = layers(lib, layersFunction);
	}

	/**
	 * Enumerates extensions.
	 */
	private static Set<String> extensions(VulkanLibrary lib, VulkanFunction<VkExtensionProperties> extensions) {
		return Arrays
				.stream(VulkanFunction.enumerate(extensions, lib, VkExtensionProperties::new))
				.map(e -> e.extensionName)
				.map(String::new)
				.collect(toSet());
	}

	/**
	 * Enumerates validation layers.
	 */
	private static Set<ValidationLayer> layers(VulkanLibrary lib, VulkanFunction<VkLayerProperties> layers) {
		return Arrays
				.stream(VulkanFunction.enumerate(layers, lib, VkLayerProperties::new))
				.map(layer -> new ValidationLayer(Native.toString(layer.layerName), layer.implementationVersion))
				.collect(toSet());
	}

	/**
	 * @return Supported extensions
	 */
	public Set<String> extensions() {
		return extensions;
	}

	/**
	 * @return Supported validation layers
	 */
	public Set<ValidationLayer> layers() {
		return layers;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("extensions", extensions)
				.append("layers", layers)
				.build();
	}
}
