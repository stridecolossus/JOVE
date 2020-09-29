package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.VkClearDepthStencilValue;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkImageSubresourceRange;
import org.sarge.jove.platform.vulkan.VkRenderPassBeginInfo;
import org.sarge.jove.platform.vulkan.VkRenderPassCreateInfo;
import org.sarge.jove.platform.vulkan.VkSubpassContents;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Render pass API.
 */
interface VulkanLibraryRenderPass {
	/**
	 * Creates a render pass.
	 * @param device			Logical device
	 * @param pCreateInfo		Descriptor
	 * @param pAllocator		Allocator
	 * @param pRenderPass		Returned render pass handle
	 * @return Result code
	 */
	int vkCreateRenderPass(Pointer device, VkRenderPassCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pRenderPass);

	/**
	 * Destroys a render pass.
	 * @param device			Logical device
	 * @param renderPass		Render pass
	 * @param pAllocator		Allocator
	 */
	void vkDestroyRenderPass(Pointer device, Pointer renderPass, Pointer pAllocator);

	/**
	 * Command - Begins a render pass.
	 * @param commandBuffer			Command buffer
	 * @param pRenderPassBegin		Descriptor
	 * @param contents				Sub-pass contents
	 */
	void vkCmdBeginRenderPass(Pointer commandBuffer, VkRenderPassBeginInfo pRenderPassBegin, VkSubpassContents contents);

	/**
	 * Command - Ends a render pass.
	 * @param commandBuffer Command buffer
	 */
	void vkCmdEndRenderPass(Pointer commandBuffer);

	/**
	 * Command - Starts the next sub-pass.
	 * @param commandBuffer			Command buffer
	 * @param contents				Sub-pass contents
	 */
	void vkCmdNextSubpass(Pointer commandBuffer, VkSubpassContents contents);

	/**
	 * Command - Draws vertices.
	 * @param commandBuffer			Command buffer
	 * @param vertexCount			Number of vertices
	 * @param instanceCount			Number of instances
	 * @param firstVertex			First vertex index
	 * @param firstInstance			First index index
	 */
	void vkCmdDraw(Pointer commandBuffer, int vertexCount, int instanceCount, int firstVertex, int firstInstance);

	/**
	 * Command - Draws indexed vertices.
	 * @param commandBuffer			Command buffer
	 * @param indexCount			Number of indices
	 * @param instanceCount			Number of instances
	 * @param vertexCount			First index
	 * @param vertexCount			Vertex offset
	 * @param firstInstance			First instance
	 */
	void vkCmdDrawIndexed(Pointer commandBuffer, int indexCount, int instanceCount, int firstIndex, int vertexOffset, int firstInstance);

	void vkCmdClearDepthStencilImage(Pointer commandBuffer, Pointer image, VkImageLayout imageLayout, VkClearDepthStencilValue pDepthStencil, int rangeCount, VkImageSubresourceRange pRanges);
}