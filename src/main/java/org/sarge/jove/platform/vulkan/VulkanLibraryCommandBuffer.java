package org.sarge.jove.platform.vulkan;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan command pool and buffer API
 * @author Sarge
 */
public interface VulkanLibraryCommandBuffer {
	/**
	 * Creates a command pool.
	 * @param device			Logical device
	 * @param pCreateInfo		Descriptor
	 * @param pAllocator		Allocator
	 * @param pCommandPool		Returned command pool
	 * @return Result code
	 */
	int vkCreateCommandPool(Pointer device, VkCommandPoolCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pCommandPool);

	/**
	 * Destroys a command pool (and its buffers).
	 * @param device			Logical device
	 * @param commandPool		Command pool
	 * @param pAllocator		Allocator
	 */
	void vkDestroyCommandPool(Pointer device, Pointer commandPool, Pointer pAllocator);

	/**
	 * Resets a command pool.
	 * @param device			Logical device
	 * @param commandPool		Command pool
	 * @param flags				Flags
	 * @return Result code
	 */
	int vkResetCommandPool(Pointer device, Pointer commandPool, int flags);

	/**
	 * Allocates a number of command buffers.
	 * @param device			Logical device
	 * @param pAllocateInfo		Descriptor
	 * @param pCommandBuffers	Returned buffer handles
	 * @return Result code
	 */
	int vkAllocateCommandBuffers(Pointer device, VkCommandBufferAllocateInfo pAllocateInfo, Pointer[] pCommandBuffers);

	/**
	 * Releases a set of command buffers back to the pool.
	 * @param device				Logical device
	 * @param commandPool			Command pool
	 * @param commandBufferCount	Number of buffers
	 * @param pCommandBuffers		Buffer handles
	 */
	void vkFreeCommandBuffers(Pointer device, Pointer commandPool, int commandBufferCount, Pointer[] pCommandBuffers);

	/**
	 * Starts recording.
	 * @param commandBuffer			Command buffer
	 * @param pBeginInfo			Descriptor
	 * @return Result code
	 */
	int vkBeginCommandBuffer(Pointer commandBuffer, VkCommandBufferBeginInfo pBeginInfo);

	/**
	 * Stops recording.
	 * @param commandBuffer Command buffer
	 * @return Result code
	 */
	int vkEndCommandBuffer(Pointer commandBuffer);

	/**
	 * Resets a command buffer.
	 * @param commandBuffer			Command buffer
	 * @param flags					Flags
	 * @return Result code
	 */
	int vkResetCommandBuffer(Pointer commandBuffer, int flags);
}
