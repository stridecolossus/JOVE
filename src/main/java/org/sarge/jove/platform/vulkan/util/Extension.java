package org.sarge.jove.platform.vulkan.util;

import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Set;

import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;

/**
 * Vulkan extensions utilities.
 * @author Sarge
 */
public final class Extension {
	private Extension() {
	}

	/**
	 * Enumerates supported extensions.
	 * @param lib		Vulkan
	 * @param func 		Extensions function
	 * @return Supported extensions
	 */
	public static Set<String> extensions(VulkanLibrary lib, VulkanFunction<VkExtensionProperties> func) {
		return Arrays
				.stream(VulkanFunction.invoke(func, lib, VkExtensionProperties::new))
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
}
