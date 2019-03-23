package org.sarge.jove.platform.vulkan;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan pipeline API.
 * @author Sarge
 */
public interface VulkanLibraryPipeline {
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

	/**
	 * Creates a pipeline layout.
	 * @param device			Logical device
	 * @param pCreateInfo		Pipeline layout descriptor
	 * @param pAllocator		Allocator
	 * @param pPipelineLayout	Returned pipeline layout handle
	 * @return Result code
	 */
	int vkCreatePipelineLayout(Pointer device, VkPipelineLayoutCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pPipelineLayout);

	/**
	 * Destroys a pipeline layout.
	 * @param device			Logical device
	 * @param pPipelineLayout	Pipeline layout
	 * @param pAllocator		Allocator
	 */
	void vkDestroyPipelineLayout(Pointer device, Pointer pipelineLayout, Pointer pAllocator);

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

	// TODO
	void vkCmdBeginRenderPass(Pointer commandBuffer, VkRenderPassBeginInfo pRenderPassBegin, int contents);
	void vkCmdNextSubpass(Pointer commandBuffer, int contents);
	void vkCmdEndRenderPass(Pointer commandBuffer);
	void vkCmdBindPipeline(Pointer commandBuffer, VkPipelineBindPoint pipelineBindPoint, Pointer pipeline);
	void vkCmdDraw(Pointer commandBuffer, int vertexCount, int instanceCount, int firstVertex, int firstInstance);

	/**
	 * Creates a graphics pipeline.
	 * @param device			Logical device
	 * @param pipelineCache		Optional pipeline cache
	 * @param createInfoCount	Number of pipelines to create
	 * @param pCreateInfos		Descriptor(s)
	 * @param pAllocator		Allocator
	 * @param pPipelines		Returned pipeline handle(s)
	 * @return Result code
	 */
	int vkCreateGraphicsPipelines(Pointer device, Pointer pipelineCache, int createInfoCount, VkGraphicsPipelineCreateInfo[] pCreateInfos, Pointer pAllocator, Pointer[] pPipelines);

	/**
	 * Destroys a pipeline.
	 * @param device			Logical device
	 * @param pipeline			Pipeline
	 * @param pAllocator		Allocator
	 */
	void vkDestroyPipeline(Pointer device, Pointer pipeline, Pointer pAllocator);

	int vkCreateFramebuffer(Pointer device, VkFramebufferCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pFramebuffer);

	void vkDestroyFramebuffer(Pointer device, Pointer framebuffer, Pointer pAllocator);
}
