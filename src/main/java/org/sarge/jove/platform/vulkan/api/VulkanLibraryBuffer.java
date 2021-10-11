package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.VkBufferCopy;
import org.sarge.jove.platform.vulkan.VkBufferCreateInfo;
import org.sarge.jove.platform.vulkan.VkIndexType;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;

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
	int vkCreateBuffer(LogicalDevice device, VkBufferCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pBuffer);

	/**
	 * Destroys a buffer.
	 * @param device			Logical device
	 * @param pBuffer			Buffer
	 * @param pAllocator		Allocator
	 * @return Result code
	 */
	void vkDestroyBuffer(DeviceContext device, VulkanBuffer buffer, Pointer pAllocator);

	/**
	 * Queries the memory requirements of the given buffer.
	 * @param device					Logical device
	 * @param pBuffer					Buffer
	 * @param pMemoryRequirements		Returned memory requirements
	 * @return Result code
	 */
	void vkGetBufferMemoryRequirements(LogicalDevice device, Pointer buffer, VkMemoryRequirements pMemoryRequirements);

	/**
	 * Binds the memory for the given buffer.
	 * @param device			Logical device
	 * @param pBuffer			Buffer
	 * @param memory			Memory
	 * @param memoryOffset		Offset
	 * @return Result code
	 */
	int vkBindBufferMemory(LogicalDevice device, Pointer buffer, DeviceMemory memory, long memoryOffset);

	/**
	 * Binds a vertex buffer.
	 * @param commandBuffer		Command
	 * @param firstBinding		First binding
	 * @param bindingCount		Number of bindings
	 * @param pBuffers			Buffer(s)
	 * @param pOffsets			Buffer offset(s)
	 */
	void vkCmdBindVertexBuffers(Command.Buffer commandBuffer, int firstBinding, int bindingCount, Pointer pBuffers, long[] pOffsets);

	/**
	 * Binds an index buffer.
	 * @param commandBuffer		Command
	 * @param buffer			Index buffer
	 * @param offset			Offset
	 * @param indexType			Index data-type
	 */
	void vkCmdBindIndexBuffer(Command.Buffer commandBuffer, VulkanBuffer buffer, long offset, VkIndexType indexType);

	/**
	 * Command to copy a buffer.
	 * @param commandBuffer		Command buffer
	 * @param srcBuffer			Source
	 * @param dstBuffer			Destination
	 * @param regionCount		Number of regions
	 * @param pRegions			Region descriptor(s)
	 */
	void vkCmdCopyBuffer(Command.Buffer commandBuffer, VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, int regionCount, VkBufferCopy[] pRegions);
}
