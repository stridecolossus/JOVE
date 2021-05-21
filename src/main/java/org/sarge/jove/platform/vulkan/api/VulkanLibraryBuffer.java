package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.common.Handle;
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
	int vkCreateBuffer(Handle device, VkBufferCreateInfo pCreateInfo, Handle pAllocator, PointerByReference pBuffer);

	/**
	 * Destroys a buffer.
	 * @param device			Logical device
	 * @param pBuffer			Buffer
	 * @param pAllocator		Allocator
	 * @return Result code
	 */
	void vkDestroyBuffer(Handle device, Handle buffer, Handle pAllocator);

	/**
	 * Queries the memory requirements of the given buffer.
	 * @param device					Logical device
	 * @param pBuffer					Buffer
	 * @param pMemoryRequirements		Returned memory requirements
	 * @return Result code
	 */
	void vkGetBufferMemoryRequirements(Handle device, Pointer buffer, VkMemoryRequirements pMemoryRequirements);

	/**
	 * Binds the memory for the given buffer.
	 * @param device			Logical device
	 * @param pBuffer			Buffer
	 * @param memory			Memory
	 * @param memoryOffset		Offset
	 * @return Result code
	 */
	int vkBindBufferMemory(Handle device, Pointer buffer, Handle memory, long memoryOffset);

	/**
	 * Binds a vertex buffer.
	 * @param commandBuffer		Command
	 * @param firstBinding		First binding
	 * @param bindingCount		Number of bindings
	 * @param pBuffers			Buffer(s)
	 * @param pOffsets			Buffer offset(s)
	 */
	void vkCmdBindVertexBuffers(Handle commandBuffer, int firstBinding, int bindingCount, Handle pBuffers, long[] pOffsets);

	/**
	 * Binds an index buffer.
	 * @param commandBuffer		Command
	 * @param buffer			Index buffer
	 * @param offset			Offset
	 * @param indexType			Index data-type
	 */
	void vkCmdBindIndexBuffer(Handle commandBuffer, Handle buffer, long offset, VkIndexType indexType);

	/**
	 * Command to copy a buffer.
	 * @param commandBuffer		Command buffer
	 * @param srcBuffer			Source
	 * @param dstBuffer			Destination
	 * @param regionCount		Number of regions
	 * @param pRegions			Region descriptor(s)
	 */
	void vkCmdCopyBuffer(Handle commandBuffer, Handle srcBuffer, Handle dstBuffer, int regionCount, VkBufferCopy[] pRegions);
}
