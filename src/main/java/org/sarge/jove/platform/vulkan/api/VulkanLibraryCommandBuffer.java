package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkCommandBufferAllocateInfo;
import org.sarge.jove.platform.vulkan.VkCommandBufferBeginInfo;
import org.sarge.jove.platform.vulkan.VkCommandPoolCreateInfo;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

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
	int vkCreateCommandPool(Handle device, VkCommandPoolCreateInfo pCreateInfo, Handle pAllocator, PointerByReference pCommandPool);

	/**
	 * Destroys a command pool (and its buffers).
	 * @param device			Logical device
	 * @param commandPool		Command pool
	 * @param pAllocator		Allocator
	 */
	void vkDestroyCommandPool(Handle device, Handle commandPool, Handle pAllocator);

	/**
	 * Resets a command pool.
	 * @param device			Logical device
	 * @param commandPool		Command pool
	 * @param flags				Flags
	 * @return Result code
	 */
	int vkResetCommandPool(Handle device, Handle commandPool, int flags);

	/**
	 * Allocates a number of command buffers.
	 * @param device			Logical device
	 * @param pAllocateInfo		Descriptor
	 * @param pCommandBuffers	Returned buffer handles
	 * @return Result code
	 */
	int vkAllocateCommandBuffers(Handle device, VkCommandBufferAllocateInfo pAllocateInfo, Pointer[] pCommandBuffers);

	/**
	 * Releases a set of command buffers back to the pool.
	 * @param device				Logical device
	 * @param commandPool			Command pool
	 * @param commandBufferCount	Number of buffers
	 * @param pCommandBuffers		Buffer handles
	 */
	void vkFreeCommandBuffers(Handle device, Handle commandPool, int commandBufferCount, Pointer[] /*HandleArray */pCommandBuffers);

	/**
	 * Starts recording.
	 * @param commandBuffer			Command buffer
	 * @param pBeginInfo			Descriptor
	 * @return Result code
	 */
	int vkBeginCommandBuffer(Handle commandBuffer, VkCommandBufferBeginInfo pBeginInfo);

	/**
	 * Stops recording.
	 * @param commandBuffer Command buffer
	 * @return Result code
	 */
	int vkEndCommandBuffer(Handle commandBuffer);

	/**
	 * Resets a command buffer.
	 * @param commandBuffer			Command buffer
	 * @param flags					Flags
	 * @return Result code
	 */
	int vkResetCommandBuffer(Handle commandBuffer, int flags);
}