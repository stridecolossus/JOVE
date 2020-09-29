package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkDeviceCreateInfo;
import org.sarge.jove.platform.vulkan.VkPresentInfoKHR;
import org.sarge.jove.platform.vulkan.VkSubmitInfo;

import com.sun.jna.ptr.PointerByReference;

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
	int vkCreateDevice(Handle physicalDevice, VkDeviceCreateInfo pCreateInfo, Handle pAllocator, PointerByReference device);

	/**
	 * Destroys a logical device.
	 * @param device				Device handle
	 * @param pAllocator			Allocator
	 * @return Result
	 */
	int vkDestroyDevice(Handle device, Handle pAllocator);

	/**
	 * Waits for the given device to become idle.
	 * @param device Logical device
	 * @return Result code
	 */
	int vkDeviceWaitIdle(Handle device);

	/**
	 * Retrieves logical device queue handle(s).
	 * @param device				Device handle
	 * @param queueFamilyIndex		Queue family index
	 * @param queueIndex			Queue index
	 * @param pQueue				Returned queue handle
	 */
	void vkGetDeviceQueue(Handle device, int queueFamilyIndex, int queueIndex, PointerByReference pQueue);

	/**
	 * Submits work to a queue.
	 * @param queue					Queue
	 * @param submitCount			Number of submissions
	 * @param pSubmits				Work submissions
	 * @param fence					Optional fence
	 * @return Result code
	 */
	int vkQueueSubmit(Handle queue, int submitCount, VkSubmitInfo[] pSubmits, Handle fence);

	/**
	 * Waits for the given queue to become idle.
	 * @param queue Queue
	 * @return Result code
	 */
	int vkQueueWaitIdle(Handle queue);

	/**
	 * Presents to the swap-chain.
	 * @param queue					Presentation queue
	 * @param pPresentInfo			Descriptor(s)
	 * @return Result code
	 */
	int vkQueuePresentKHR(Handle queue, VkPresentInfoKHR[] pPresentInfo);
}