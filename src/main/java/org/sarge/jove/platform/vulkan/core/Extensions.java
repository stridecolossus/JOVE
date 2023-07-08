package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.toSet;

import java.util.*;

import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;

import com.sun.jna.ptr.IntByReference;

/**
 * Helper class for retrieving extensions supported by the Vulkan platform or a physical device.
 * @author Sarge
 */
public final class Extensions {
	private Extensions() {
	}

	/**
	 * Enumerates extensions supported by this platform.
	 * @param lib		Vulkan
	 * @param count		Count
	 * @return Extensions supported by this platform
	 */
	public static Set<String> extensions(VulkanLibrary lib, IntByReference count) {
		final VulkanFunction<VkExtensionProperties> func = (ref, array) -> lib.vkEnumerateInstanceExtensionProperties(null, ref, array);
		return extensions(count, func);
	}

	/**
	 * Enumerates supported extensions for the Vulkan platform or a physical device.
	 * @param count			Count
	 * @param function 		Extensions function
	 * @return Supported extensions
	 */
	static Set<String> extensions(IntByReference count, VulkanFunction<VkExtensionProperties> function) {
		return Arrays
				.stream(VulkanFunction.invoke(function, count, new VkExtensionProperties()))
				.map(e -> e.extensionName)
				.map(String::new)
				.collect(toSet());
	}
}
