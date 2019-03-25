package org.sarge.jove.platform.vulkan;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan API for device management.
 * @author Sarge
 */
interface VulkanLibraryDevice {
	/**
	 * Enumerates the physical devices on this platform.
	 * @param instance		Vulkan instance
	 * @param count			Number of devices
	 * @param devices		Device handles
	 * @return Result
	 */
	int vkEnumeratePhysicalDevices(Pointer instance, IntByReference count, Pointer[] devices);

	/**
	 * Retrieves the properties of the given physical device.
	 * @param device		Device handle
	 * @param props			Properties
	 */
	void vkGetPhysicalDeviceProperties(Pointer device, VkPhysicalDeviceProperties props);

	// TODO
	void vkGetPhysicalDeviceMemoryProperties(Pointer device, VkPhysicalDeviceMemoryProperties pMemoryProperties);

	/**
	 * Retrieves the features of the given physical device.
	 * @param device		Device handle
	 * @param features		Features
	 */
	void vkGetPhysicalDeviceFeatures(Pointer device, VkPhysicalDeviceFeatures features);

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
	int vkEnumerateDeviceExtensionProperties(Pointer device, String layer, IntByReference count, VkExtensionProperties extensions);

	/**
	 * Enumerates device-specific validation layers.
	 * @param device		Physical device handle
	 * @param count			Number of layers
	 * @param extensions	Returned layers
	 * @return Result
	 */
	int vkEnumerateDeviceLayerProperties(Pointer device, IntByReference count, VkLayerProperties layers);

	/**
	 * Creates a logical device.
	 * @param physicalDevice		Physical device handle
	 * @param pCreateInfo			Device descriptor
	 * @param pAllocator			Allocator
	 * @param device				Returned logical device handle
	 * @return Result
	 */
	int vkCreateDevice(Pointer physicalDevice, VkDeviceCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference device);

	/**
	 * Destroys a logical device.
	 * @param device				Device handle
	 * @param pAllocator			Allocator
	 * @return Result
	 */
	int vkDestroyDevice(Pointer device, Pointer pAllocator);

	/**
	 * Retrieves logical device queue handle(s).
	 * @param device				Device handle
	 * @param queueFamilyIndex		Queue family index
	 * @param queueIndex			Queue index
	 * @param pQueue				Returned queue handle
	 */
	void vkGetDeviceQueue(Pointer device, int queueFamilyIndex, int queueIndex, PointerByReference pQueue);

	int vkQueueSubmit(Pointer queue, int submitCount, VkSubmitInfo[] pSubmits, Pointer fence);
	int vkQueueWaitIdle(Pointer queue);
	int vkDeviceWaitIdle(Pointer device);
	int vkQueuePresentKHR(Pointer queue, VkPresentInfoKHR[] pPresentInfo);
}
