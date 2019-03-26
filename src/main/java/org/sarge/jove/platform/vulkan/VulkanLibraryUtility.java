package org.sarge.jove.platform.vulkan;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan utilities API.
 * @author Sarge
 */
interface VulkanLibraryUtility extends VulkanLibrarySynchronize, VulkanLibraryShader, VulkanLibraryBuffer, VulkanLibraryMemory {
	// Aggregate interface
}

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

/**
 * Vulkan shader module API.
 */
interface VulkanLibraryShader {
	/**
	 * Create a shader.
	 * @param device			Logical device
	 * @param info				Shader descriptor
	 * @param pAllocator		Allocator
	 * @param shader			Returned shader handle
	 * @return Result code
	 */
	int vkCreateShaderModule(Pointer device, VkShaderModuleCreateInfo info, Pointer pAllocator, PointerByReference shader);

	/**
	 * Destroys a shader.
	 * @param device			Logical device
	 * @param shader			Shader
	 * @param pAllocator		Allocator
	 */
	void vkDestroyShaderModule(Pointer device, Pointer shader, Pointer pAllocator);
}

/**
 * Vulkan buffer API.
 */
interface VulkanLibraryBuffer {
	// TODO
	int vkCreateBuffer(Pointer device, VkBufferCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pBuffer);
	void vkDestroyBuffer(Pointer device, Pointer buffer, Pointer pAllocator);

	void vkGetBufferMemoryRequirements(Pointer device, Pointer buffer, VkMemoryRequirements pMemoryRequirements);

	int vkBindBufferMemory(Pointer device, Pointer buffer, Pointer memory, long memoryOffset);

	//void vkCmdBindIndexBuffer(Pointer commandBuffer, Pointer buffer, long offset, VkIndexType indexType);
	void vkCmdBindVertexBuffers(Pointer commandBuffer, int firstBinding, int bindingCount, Pointer[] pBuffers, long[] pOffsets);
}

/**
 * Vulkan memory API.
 */
interface VulkanLibraryMemory {
	int vkAllocateMemory(Pointer device, VkMemoryAllocateInfo pAllocateInfo, Pointer pAllocator, PointerByReference pMemory);
	void vkFreeMemory(Pointer device, Pointer memory, Pointer pAllocator);

	//int vkBindImageMemory(Pointer device, VkImage image, VkDeviceMemory memory, VkDeviceSize memoryOffset);

	int vkMapMemory(Pointer device, Pointer memory, long offset, long size, int flags, PointerByReference ppData);
	void vkUnmapMemory(Pointer device, Pointer memory);
}
