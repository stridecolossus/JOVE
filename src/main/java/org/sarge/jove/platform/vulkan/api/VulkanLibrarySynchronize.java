package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.VkFenceCreateInfo;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.VkSemaphoreCreateInfo;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.Fence;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.Semaphore;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan synchronisation API.
 */
interface VulkanLibrarySynchronize {
	/**
	 * Creates a semaphore.
	 * @param device			Device
	 * @param pCreateInfo		Descriptor
	 * @param pAllocator		Allocator
	 * @param pSemaphore		Returned semaphore
	 * @return Result code
	 */
	int vkCreateSemaphore(LogicalDevice device, VkSemaphoreCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pSemaphore);

	/**
	 * Destroys a semaphore.
	 * @param device			Device
	 * @param semaphore			Semaphore
	 * @param pAllocator		Allocator
	 */
	void vkDestroySemaphore(DeviceContext device, Semaphore semaphore, Pointer pAllocator);

	/**
	 * Creates a fence.
	 * @param device			Device
	 * @param pCreateInfo		Descriptor
	 * @param pAllocator		Allocator
	 * @param pSemaphore		Returned fence
	 * @return Result code
	 */
	int vkCreateFence(LogicalDevice device, VkFenceCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pFence);

	/**
	 * Destroys a fence.
	 * @param device			Device
	 * @param fence				Fence
	 * @param pAllocator		Allocator
	 */
	void vkDestroyFence(DeviceContext device, Fence fence, Pointer pAllocator);

	/**
	 * Resets a number of fences.
	 * @param device			Device
	 * @param fenceCount		Number of fences
	 * @param pFences			Fences
	 * @return Result code
	 */
	int vkResetFences(DeviceContext device, int fenceCount, Fence[] pFences);

	/**
	 * Retrieves the status of a given fence.
	 * @param device
	 * @param fence
	 * @return Fence status flag
	 * @see VkResult
	 */
	int vkGetFenceStatus(DeviceContext device, Fence fence);

	/**
	 * Waits for a number of fences.
	 * @param device			Device
	 * @param fenceCount		Number of fences
	 * @param pFences			Fences
	 * @param waitAll			Whether to wait for <b>all</b> fences or <b>any</b>
	 * @param timeout			Timeout or {@link Long#MAX_VALUE}
	 * @return Result code
	 */
	int vkWaitForFences(DeviceContext device, int fenceCount, Fence[] pFences, VulkanBoolean waitAll, long timeout);
}

