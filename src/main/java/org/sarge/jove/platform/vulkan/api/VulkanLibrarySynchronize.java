package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.VkFenceCreateInfo;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.VkSemaphoreCreateInfo;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

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
	int vkCreateSemaphore(Pointer device, VkSemaphoreCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pSemaphore);

	/**
	 * Destroys a semaphore.
	 * @param device			Device
	 * @param semaphore			Semaphore
	 * @param pAllocator		Allocator
	 */
	void vkDestroySemaphore(Pointer device, Pointer semaphore, Pointer pAllocator);

	/**
	 * Creates a fence.
	 * @param device			Device
	 * @param pCreateInfo		Descriptor
	 * @param pAllocator		Allocator
	 * @param pSemaphore		Returned fence
	 * @return Result code
	 */
	int vkCreateFence(Pointer device, VkFenceCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pFence);

	/**
	 * Destroys a fence.
	 * @param device			Device
	 * @param fence				Fence
	 * @param pAllocator		Allocator
	 */
	void vkDestroyFence(Pointer device, Pointer fence, Pointer pAllocator);

	/**
	 * Resets a number of fences.
	 * @param device			Device
	 * @param fenceCount		Number of fences
	 * @param pFences			Fences
	 * @return Result code
	 */
	int vkResetFences(Pointer device, int fenceCount, Pointer[] pFences);

	/**
	 * Retrieves the status of a given fence.
	 * @param device
	 * @param fence
	 * @return Fence status flag
	 * @see VkResult
	 */
	int vkGetFenceStatus(Pointer device, Pointer fence);

	/**
	 * Waits for a number of fences.
	 * @param device			Device
	 * @param fenceCount		Number of fences
	 * @param pFences			Fences
	 * @param waitAll			Whether to wait for <b>all</b> fences or <b>any</b>
	 * @param timeout			Timeout or {@link Long#MAX_VALUE}
	 * @return Result code
	 */
	int vkWaitForFences(Pointer device, int fenceCount, Pointer[] pFences, VulkanBoolean waitAll, long timeout);
}