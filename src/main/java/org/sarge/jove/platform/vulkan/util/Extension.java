package org.sarge.jove.platform.vulkan.util;

import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Set;

import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;

import com.sun.jna.ptr.IntByReference;

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
	 * @param count		Count
	 * @param func 		Extensions function
	 * @return Supported extensions
	 */
	public static Set<String> extensions(VulkanLibrary lib, IntByReference count, VulkanFunction<VkExtensionProperties> func) {
		return Arrays
				.stream(VulkanFunction.invoke(func, count, VkExtensionProperties::new))
				.map(e -> e.extensionName)
				.map(String::new)
				.collect(toSet());
	}

	/**
	 * Enumerates extensions supported by this platform.
	 * @param lib		Vulkan
	 * @param count		Count
	 * @return Extensions supported by this platform
	 */
	static Set<String> extensions(VulkanLibrary lib, IntByReference count) {
		final VulkanFunction<VkExtensionProperties> func = (ref, array) -> lib.vkEnumerateInstanceExtensionProperties(null, ref, array);
		return extensions(lib, count, func);
	}
}
