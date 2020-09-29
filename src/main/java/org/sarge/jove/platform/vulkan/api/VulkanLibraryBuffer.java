package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.VkBufferCopy;
import org.sarge.jove.platform.vulkan.VkBufferCreateInfo;
import org.sarge.jove.platform.vulkan.VkIndexType;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

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