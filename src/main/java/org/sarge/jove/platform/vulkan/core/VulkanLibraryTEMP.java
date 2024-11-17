package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.common.Version;

/**
 * The <i>Vulkan library</i> is an aggregated interface defining the Vulkan API.
 * @author Sarge
 */
public interface VulkanLibraryTEMP extends Instance.Library, PhysicalDevice.Library {
	/**
	 * Vulkan API version.
	 */
	Version VERSION = new Version(1, 1, 0);

	/**
	 * Successful result code.
	 */
	int SUCCESS = VkResult.SUCCESS.value();

	// TODO...
}
