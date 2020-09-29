package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.VkBufferMemoryBarrier;
import org.sarge.jove.platform.vulkan.VkGraphicsPipelineCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageMemoryBarrier;
import org.sarge.jove.platform.vulkan.VkMemoryBarrier;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkPipelineLayoutCreateInfo;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan pipeline API.
 */
interface VulkanLibraryPipeline {
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

	/**
	 * Command to bind a pipeline.
	 * @param commandBuffer			Command buffer
	 * @param pipelineBindPoint		Bind-point
	 * @param pipeline				Pipeline to bind
	 */
	void vkCmdBindPipeline(Pointer commandBuffer, VkPipelineBindPoint pipelineBindPoint, Pointer pipeline);

	void vkCmdPipelineBarrier(Pointer commandBuffer, int srcStageMask, int dstStageMask, int dependencyFlags, int memoryBarrierCount, VkMemoryBarrier[] pMemoryBarriers, int bufferMemoryBarrierCount, VkBufferMemoryBarrier[] pBufferMemoryBarriers, int imageMemoryBarrierCount, VkImageMemoryBarrier[] pImageMemoryBarriers);
}