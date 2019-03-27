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
	/**
	 * Creates a buffer.
	 * @param device			Logical device
	 * @param pCreateInfo		Descriptor
	 * @param pAllocator		Allocator
	 * @param pBuffer			Returned buffer
	 * @return Result code
	 */
	int vkCreateBuffer(Pointer device, VkBufferCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pBuffer);

	/**
	 * Destroys a buffer.
	 * @param device			Logical device
	 * @param pBuffer			Buffer
	 * @param pAllocator		Allocator
	 * @return Result code
	 */
	void vkDestroyBuffer(Pointer device, Pointer buffer, Pointer pAllocator);

	/**
	 * Queries the memory requirements of the given buffer.
	 * @param device					Logical device
	 * @param pBuffer					Buffer
	 * @param pMemoryRequirements		Returned memory requirements
	 * @return Result code
	 */
	void vkGetBufferMemoryRequirements(Pointer device, Pointer buffer, VkMemoryRequirements pMemoryRequirements);

	/**
	 * Binds the memory for the given buffer.
	 * @param device			Logical device
	 * @param pBuffer			Buffer
	 * @param memory			Memory
	 * @param memoryOffset		Offset
	 * @return Result code
	 */
	int vkBindBufferMemory(Pointer device, Pointer buffer, Pointer memory, long memoryOffset);

	/**
	 * Binds a vertex buffer.
	 * @param commandBuffer		Command
	 * @param firstBinding		First binding
	 * @param bindingCount		Number of bindings
	 * @param pBuffers			Buffer(s)
	 * @param pOffsets			Buffer offset(s)
	 */
	void vkCmdBindVertexBuffers(Pointer commandBuffer, int firstBinding, int bindingCount, Pointer[] pBuffers, long[] pOffsets);

	/**
	 * Binds an index buffer.
	 * @param commandBuffer		Command
	 * @param buffer			Index buffer
	 * @param offset			Offset
	 * @param indexType			Index data-type
	 */
	void vkCmdBindIndexBuffer(Pointer commandBuffer, Pointer buffer, long offset, VkIndexType indexType);

	/**
	 * Command to copy a buffer.
	 * @param commandBuffer		Command buffer
	 * @param srcBuffer			Source
	 * @param dstBuffer			Destination
	 * @param regionCount		Number of regions
	 * @param pRegions			Region descriptor(s)
	 */
	void vkCmdCopyBuffer(Pointer commandBuffer, Pointer srcBuffer, Pointer dstBuffer, int regionCount, VkBufferCopy[] pRegions);
}

/**
 * Vulkan memory API.
 */
interface VulkanLibraryMemory {
	/**
	 * Allocates memory.
	 * @param device			Logical device
	 * @param pAllocateInfo		Memory descriptor
	 * @param pAllocator		Allocator
	 * @param pMemory			Returned memory handle
	 * @return Result code
	 */
	int vkAllocateMemory(Pointer device, VkMemoryAllocateInfo pAllocateInfo, Pointer pAllocator, PointerByReference pMemory);

	/**
	 * Releases memory.
	 * @param device			Logical device
	 * @param memory			Memory
	 * @param pAllocator		Allocator
	 */
	void vkFreeMemory(Pointer device, Pointer memory, Pointer pAllocator);

	//int vkBindImageMemory(Pointer device, VkImage image, VkDeviceMemory memory, VkDeviceSize memoryOffset);

	/**
	 * Maps buffer memory.
	 * @param device			Logical device
	 * @param memory			Buffer memory
	 * @param offset			Offset
	 * @param size				Data length
	 * @param flags				Flags
	 * @param ppData			Returned memory pointer
	 * @return Result code
	 */
	int vkMapMemory(Pointer device, Pointer memory, long offset, long size, int flags, PointerByReference ppData);

	/**
	 * Un-maps buffer memory.
	 * @param device			Logical device
	 * @param memory			Buffer memory
	 */
	void vkUnmapMemory(Pointer device, Pointer memory);
}
