package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.VkLayerProperties;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceMemoryProperties;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceProperties;
import org.sarge.jove.platform.vulkan.VkQueueFamilyProperties;

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
	int vkEnumeratePhysicalDevices(Handle instance, IntByReference count, Pointer[] devices);

	/**
	 * Retrieves the properties of the given physical device.
	 * @param device		Device handle
	 * @param props			Properties
	 */
	void vkGetPhysicalDeviceProperties(Handle device, VkPhysicalDeviceProperties props);

	/**
	 * Retrieves the memory properties of the given physical device.
	 * @param device				Device
	 * @param pMemoryProperties		Returned memory properties
	 */
	void vkGetPhysicalDeviceMemoryProperties(Handle device, VkPhysicalDeviceMemoryProperties pMemoryProperties);

	/**
	 * Retrieves the features of the given physical device.
	 * @param device		Device handle
	 * @param features		Features
	 */
	void vkGetPhysicalDeviceFeatures(Handle device, VkPhysicalDeviceFeatures features);

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
	int vkEnumerateDeviceExtensionProperties(Handle device, String layer, IntByReference count, VkExtensionProperties extensions);

	/**
	 * Enumerates device-specific validation layers.
	 * @param device		Physical device handle
	 * @param count			Number of layers
	 * @param extensions	Returned layers
	 * @return Result
	 */
	int vkEnumerateDeviceLayerProperties(Handle device, IntByReference count, VkLayerProperties layers);
}