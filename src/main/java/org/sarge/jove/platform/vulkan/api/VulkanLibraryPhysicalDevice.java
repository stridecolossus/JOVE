package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkFormatProperties;
import org.sarge.jove.platform.vulkan.VkLayerProperties;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceMemoryProperties;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceProperties;
import org.sarge.jove.platform.vulkan.VkQueueFamilyProperties;
import org.sarge.jove.platform.vulkan.core.Instance;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * Vulkan physical device API.
 */
interface VulkanLibraryPhysicalDevice {
	/**
	 * Enumerates the physical devices on this platform.
	 * @param instance		Vulkan instance
	 * @param count			Number of devices
	 * @param devices		Device handles
	 * @return Result
	 */
	int vkEnumeratePhysicalDevices(Instance instance, IntByReference count, Pointer[] devices);

	/**
	 * Retrieves the properties of the given physical device.
	 * @param device		Device handle
	 * @param props			Properties
	 */
	void vkGetPhysicalDeviceProperties(PhysicalDevice device, VkPhysicalDeviceProperties props);

	/**
	 * Retrieves the memory properties of the given physical device.
	 * @param device				Device
	 * @param pMemoryProperties		Returned memory properties
	 */
	void vkGetPhysicalDeviceMemoryProperties(PhysicalDevice device, VkPhysicalDeviceMemoryProperties pMemoryProperties);

	/**
	 * Retrieves the features of the given physical device.
	 * @param device		Device handle
	 * @param features		Features
	 */
	void vkGetPhysicalDeviceFeatures(PhysicalDevice device, VkPhysicalDeviceFeatures features);

	/**
	 * Enumerates the queue families of a device.
	 * @param device		Device handle
	 * @param count			Number of devices
	 * @param props			Queue family properties
	 */
	void vkGetPhysicalDeviceQueueFamilyProperties(Pointer device, IntByReference count, VkQueueFamilyProperties props);

	/**
	 * Enumerates device-specific extension properties.
	 * @param device		Physical device handle
	 * @param layer			Layer name or <tt>null</tt> for all
	 * @param count			Number of extensions
	 * @param extensions	Returned extensions
	 * @return Result
	 */
	int vkEnumerateDeviceExtensionProperties(PhysicalDevice device, String layer, IntByReference count, VkExtensionProperties extensions);

	/**
	 * Enumerates device-specific validation layers.
	 * @param device		Physical device handle
	 * @param count			Number of layers
	 * @param extensions	Returned layers
	 * @return Result
	 */
	int vkEnumerateDeviceLayerProperties(PhysicalDevice device, IntByReference count, VkLayerProperties layers);

	/**
	 * Retrieves supported properties of the given format.
	 * @param device		Physical device handle
	 * @param format		Format
	 * @param props			Format properties
	 */
	void vkGetPhysicalDeviceFormatProperties(PhysicalDevice device, VkFormat format, VkFormatProperties props);
}
