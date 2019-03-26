package org.sarge.jove.platform.vulkan;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan API for instances, devices and command buffers.
 * @author Sarge
 */
interface VulkanLibrarySystem extends VulkanLibraryInstance, VulkanLibraryPhysicalDevice, VulkanLibraryLogicalDevice, VulkanLibraryCommandBuffer {
	// Aggregate interface
}

/**
 * Vulkan API for instance management.
 */
interface VulkanLibraryInstance {
	/**
	 * Creates a vulkan instance.
	 * @param info			Instance descriptor
	 * @param allocator		Allocator
	 * @param instance		Returned instance
	 * @return Result
	 */
	int vkCreateInstance(VkInstanceCreateInfo info, Pointer allocator, PointerByReference instance);

	/**
	 * Destroys the vulkan instance.
	 * @param instance		Instance handle
	 * @param allocator		Allocator
	 */
	void vkDestroyInstance(Pointer instance, Pointer allocator);

	/**
	 * Enumerates extension properties.
	 * @param filter		Layer name or <tt>null</tt> for all
	 * @param count			Number of extensions
	 * @param extensions	Extensions
	 * @return Result
	 */
	int vkEnumerateInstanceExtensionProperties(String pLayerName, IntByReference count, VkExtensionProperties extensions);

	/**
	 * Enumerates validation layer properties.
	 * @param count			Number of layers
	 * @param layers		Layers
	 * @return Result
	 */
	int vkEnumerateInstanceLayerProperties(IntByReference count, VkLayerProperties layers);
}

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
}

/**
 * Vulkan logical device API.
 */
interface VulkanLibraryLogicalDevice {
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

	int vkDeviceWaitIdle(Pointer device);

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
	int vkQueuePresentKHR(Pointer queue, VkPresentInfoKHR[] pPresentInfo);
}

/**
 * Vulkan command pool and buffer API.
 */
interface VulkanLibraryCommandBuffer {
	/**
	 * Creates a command pool.
	 * @param device			Logical device
	 * @param pCreateInfo		Descriptor
	 * @param pAllocator		Allocator
	 * @param pCommandPool		Returned command pool
	 * @return Result code
	 */
	int vkCreateCommandPool(Pointer device, VkCommandPoolCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pCommandPool);

	/**
	 * Destroys a command pool (and its buffers).
	 * @param device			Logical device
	 * @param commandPool		Command pool
	 * @param pAllocator		Allocator
	 */
	void vkDestroyCommandPool(Pointer device, Pointer commandPool, Pointer pAllocator);

	/**
	 * Resets a command pool.
	 * @param device			Logical device
	 * @param commandPool		Command pool
	 * @param flags				Flags
	 * @return Result code
	 */
	int vkResetCommandPool(Pointer device, Pointer commandPool, int flags);

	/**
	 * Allocates a number of command buffers.
	 * @param device			Logical device
	 * @param pAllocateInfo		Descriptor
	 * @param pCommandBuffers	Returned buffer handles
	 * @return Result code
	 */
	int vkAllocateCommandBuffers(Pointer device, VkCommandBufferAllocateInfo pAllocateInfo, Pointer[] pCommandBuffers);

	/**
	 * Releases a set of command buffers back to the pool.
	 * @param device				Logical device
	 * @param commandPool			Command pool
	 * @param commandBufferCount	Number of buffers
	 * @param pCommandBuffers		Buffer handles
	 */
	void vkFreeCommandBuffers(Pointer device, Pointer commandPool, int commandBufferCount, Pointer[] pCommandBuffers);

	/**
	 * Starts recording.
	 * @param commandBuffer			Command buffer
	 * @param pBeginInfo			Descriptor
	 * @return Result code
	 */
	int vkBeginCommandBuffer(Pointer commandBuffer, VkCommandBufferBeginInfo pBeginInfo);

	/**
	 * Stops recording.
	 * @param commandBuffer Command buffer
	 * @return Result code
	 */
	int vkEndCommandBuffer(Pointer commandBuffer);

	/**
	 * Resets a command buffer.
	 * @param commandBuffer			Command buffer
	 * @param flags					Flags
	 * @return Result code
	 */
	int vkResetCommandBuffer(Pointer commandBuffer, int flags);
}
