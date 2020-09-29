package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.common.Handle;
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
	int vkCreatePipelineLayout(Handle device, VkPipelineLayoutCreateInfo pCreateInfo, Handle pAllocator, PointerByReference pPipelineLayout);

	/**
	 * Destroys a pipeline layout.
	 * @param device			Logical device
	 * @param pPipelineLayout	Pipeline layout
	 * @param pAllocator		Allocator
	 */
	void vkDestroyPipelineLayout(Handle device, Handle pipelineLayout, Handle pAllocator);

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
	int vkCreateGraphicsPipelines(Handle device, Handle pipelineCache, int createInfoCount, VkGraphicsPipelineCreateInfo[] pCreateInfos, Handle pAllocator, Pointer[] pPipelines);

	/**
	 * Destroys a pipeline.
	 * @param device			Logical device
	 * @param pipeline			Pipeline
	 * @param pAllocator		Allocator
	 */
	void vkDestroyPipeline(Handle device, Handle pipeline, Handle pAllocator);

	/**
	 * Command to bind a pipeline.
	 * @param commandBuffer			Command buffer
	 * @param pipelineBindPoint		Bind-point
	 * @param pipeline				Pipeline to bind
	 */
	void vkCmdBindPipeline(Handle commandBuffer, VkPipelineBindPoint pipelineBindPoint, Handle pipeline);

	void vkCmdPipelineBarrier(Handle commandBuffer, int srcStageMask, int dstStageMask, int dependencyFlags, int memoryBarrierCount, VkMemoryBarrier[] pMemoryBarriers, int bufferMemoryBarrierCount, VkBufferMemoryBarrier[] pBufferMemoryBarriers, int imageMemoryBarrierCount, VkImageMemoryBarrier[] pImageMemoryBarriers);
}