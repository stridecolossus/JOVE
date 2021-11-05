package org.sarge.jove.platform.vulkan.api;

import java.nio.ByteBuffer;

import org.sarge.jove.platform.vulkan.VkBufferMemoryBarrier;
import org.sarge.jove.platform.vulkan.VkGraphicsPipelineCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageMemoryBarrier;
import org.sarge.jove.platform.vulkan.VkMemoryBarrier;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkPipelineCacheCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineLayoutCreateInfo;
import org.sarge.jove.platform.vulkan.common.Command.Buffer;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline;
import org.sarge.jove.platform.vulkan.pipeline.PipelineCache;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
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
	int vkCreatePipelineLayout(DeviceContext device, VkPipelineLayoutCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pPipelineLayout);

	/**
	 * Destroys a pipeline layout.
	 * @param device			Logical device
	 * @param pPipelineLayout	Pipeline layout
	 * @param pAllocator		Allocator
	 */
	void vkDestroyPipelineLayout(DeviceContext device, PipelineLayout pipelineLayout, Pointer pAllocator);

	/**
	 * Creates a pipeline cache.
	 * @param device			Logical device
	 * @param pCreateInfo		Pipeline cache descriptor
	 * @param pAllocator		Allocator
	 * @param pPipelineCache	Returned pipeline cache handle
	 * @return Result code
	 */
	int vkCreatePipelineCache(DeviceContext device, VkPipelineCacheCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pPipelineCache);

	/**
	 * Merges pipeline caches.
	 * @param device			Logical device
	 * @param dstCache			Target cache
	 * @param srcCacheCount		Number of caches to merge
	 * @param pSrcCaches		Array of caches to merge
	 * @return Result code
	 */
	int vkMergePipelineCaches(DeviceContext device, PipelineCache dstCache, int srcCacheCount, Pointer pSrcCaches);

	/**
	 * Retrieves a pipeline cache.
	 * @param device			Logical device
	 * @param cache				Pipeline cache
	 * @param pDataSize			Cache size
	 * @param pData				Cache data
	 * @return Result code
	 */
	int vkGetPipelineCacheData(DeviceContext device, PipelineCache cache, IntByReference pDataSize, ByteBuffer pData);

	/**
	 * Destroys a pipeline cache.
	 * @param device			Logical device
	 * @param cache				Pipeline cache to destroy
	 * @param pAllocator		Allocator
	 */
	void vkDestroyPipelineCache(DeviceContext device, PipelineCache cache, Pointer pAllocator);

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
	int vkCreateGraphicsPipelines(LogicalDevice device, PipelineCache pipelineCache, int createInfoCount, VkGraphicsPipelineCreateInfo[] pCreateInfos, Pointer pAllocator, Pointer[] pPipelines);

	/**
	 * Destroys a pipeline.
	 * @param device			Logical device
	 * @param pipeline			Pipeline
	 * @param pAllocator		Allocator
	 */
	void vkDestroyPipeline(DeviceContext device, Pipeline pipeline, Pointer pAllocator);

	/**
	 * Command to bind a pipeline.
	 * @param commandBuffer			Command buffer
	 * @param pipelineBindPoint		Bind-point
	 * @param pipeline				Pipeline to bind
	 */
	void vkCmdBindPipeline(Buffer commandBuffer, VkPipelineBindPoint pipelineBindPoint, Pipeline pipeline);

	/**
	 * Command to apply a pipeline barrier.
	 * @param commandBuffer
	 * @param srcStageMask
	 * @param dstStageMask
	 * @param dependencyFlags
	 * @param memoryBarrierCount
	 * @param pMemoryBarriers
	 * @param bufferMemoryBarrierCount
	 * @param pBufferMemoryBarriers
	 * @param imageMemoryBarrierCount
	 * @param pImageMemoryBarriers
	 */
	void vkCmdPipelineBarrier(Buffer commandBuffer, int srcStageMask, int dstStageMask, int dependencyFlags, int memoryBarrierCount, VkMemoryBarrier[] pMemoryBarriers, int bufferMemoryBarrierCount, VkBufferMemoryBarrier[] pBufferMemoryBarriers, int imageMemoryBarrierCount, VkImageMemoryBarrier[] pImageMemoryBarriers);

	/**
	 * Updates pipeline push constants.
	 * @param commandBuffer			Command buffer
	 * @param layout				Pipeline layout
	 * @param stageFlags			Stage flags (mask)
	 * @param offset				Start of the range (bytes)
	 * @param size					Size of the push constants (bytes)
	 * @param pValues				Push constants as an array of bytes
	 */
	void vkCmdPushConstants(Buffer commandBuffer, PipelineLayout layout, int stageFlags, int offset, int size, byte[] pValues);
}
