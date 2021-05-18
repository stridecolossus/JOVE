package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Set;

import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.VkLayerProperties;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;

/**
 * Helper methods for supported extensions and validation layers.
 * @author Sarge
 */
final class Support {
	private Support() {
	}

	/**
	 * Enumerates supported extensions.
	 * @param lib		Vulkan
	 * @param func 		Extensions function
	 * @return Supported extensions
	 */
	static Set<String> extensions(VulkanLibrary lib, VulkanFunction<VkExtensionProperties> func) {
		return Arrays
				.stream(VulkanFunction.enumerate(func, lib, VkExtensionProperties::new))
				.map(e -> e.extensionName)
				.map(String::new)
				.collect(toSet());
	}

	/**
	 * @param lib Vulkan
	 * @return Extensions supported by this platform
	 */
	static Set<String> extensions(VulkanLibrary lib) {
		final VulkanFunction<VkExtensionProperties> func = (api, count, array) -> api.vkEnumerateInstanceExtensionProperties(null, count, array);
		return extensions(lib, func);
	}

	/**
	 * @param lib Vulkan library
	 * @return Validation layers supported by this platform
	 */
	static Set<ValidationLayer> layers(VulkanLibrary lib) {
		final VulkanFunction<VkLayerProperties> func = (api, count, array) -> api.vkEnumerateInstanceLayerProperties(count, array);
		return ValidationLayer.enumerate(lib, func);
	}
}
