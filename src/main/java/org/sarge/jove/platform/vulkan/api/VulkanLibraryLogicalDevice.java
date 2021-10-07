package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.VkDeviceCreateInfo;
import org.sarge.jove.platform.vulkan.VkSubmitInfo;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.core.Fence;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice;

import com.sun.jna.Pointer;
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
	int vkCreateDevice(PhysicalDevice physicalDevice, VkDeviceCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference device);

	/**
	 * Destroys a logical device.
	 * @param device				Device handle
	 * @param pAllocator			Allocator
	 * @return Result
	 */
	void vkDestroyDevice(LogicalDevice device, Pointer pAllocator);

	/**
	 * Waits for the given device to become idle.
	 * @param device Logical device
	 * @return Result code
	 */
	int vkDeviceWaitIdle(LogicalDevice device);

	/**
	 * Retrieves logical device queue handle(s).
	 * @param device				Device handle
	 * @param queueFamilyIndex		Queue family index
	 * @param queueIndex			Queue index
	 * @param pQueue				Returned queue handle
	 */
	void vkGetDeviceQueue(Pointer device, int queueFamilyIndex, int queueIndex, PointerByReference pQueue);

	/**
	 * Submits work to a queue.
	 * @param queue					Queue
	 * @param submitCount			Number of submissions
	 * @param pSubmits				Work submissions
	 * @param fence					Optional fence
	 * @return Result code
	 */
	int vkQueueSubmit(Queue queue, int submitCount, VkSubmitInfo[] pSubmits, Fence fence);

	/**
	 * Waits for the given queue to become idle.
	 * @param queue Queue
	 * @return Result code
	 */
	int vkQueueWaitIdle(Queue queue);
}
