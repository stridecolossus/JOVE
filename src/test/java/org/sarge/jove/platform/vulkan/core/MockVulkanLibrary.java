package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeReference;
import org.sarge.jove.foreign.NativeReference.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.*;
import org.sarge.jove.platform.vulkan.core.Query.Pool;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.pipeline.*;
import org.sarge.jove.platform.vulkan.render.*;
import org.sarge.jove.platform.vulkan.render.DescriptorSet.Layout;
import org.sarge.jove.util.EnumMask;

public class MockVulkanLibrary implements VulkanLibrary {
	@Override
	public VkResult vkCreateInstance(VkInstanceCreateInfo pCreateInfo, Handle pAllocator, Pointer pInstance) {
		return VkResult.SUCCESS;
	}

	@Override
	public void vkDestroyInstance(Instance instance, Handle pAllocator) {
	}

	@Override
	public Handle vkGetInstanceProcAddr(Instance instance, String pName) {
		return null;
	}

	@Override
	public VkResult vkEnumeratePhysicalDevices(Instance instance, NativeReference<Integer> pPhysicalDeviceCount, Handle[] devices) {
		return VkResult.SUCCESS;
	}

	@Override
	public void vkGetPhysicalDeviceProperties(PhysicalDevice device, VkPhysicalDeviceProperties props) {
	}

	@Override
	public void vkGetPhysicalDeviceFeatures(Handle device, VkPhysicalDeviceFeatures features) {
	}

	@Override
	public void vkGetPhysicalDeviceQueueFamilyProperties(Handle device, IntegerReference pQueueFamilyPropertyCount, VkQueueFamilyProperties[] props) {
	}

	@Override
	public void vkGetPhysicalDeviceFormatProperties(PhysicalDevice device, VkFormat format, VkFormatProperties props) {
	}

	@Override
	public VkResult vkGetPhysicalDeviceSurfaceSupportKHR(PhysicalDevice device, int queueFamilyIndex, Handle surface, IntegerReference supported) {
		return VkResult.SUCCESS;
	}

	@Override
	public VkResult vkCreateDevice(PhysicalDevice physicalDevice, VkDeviceCreateInfo pCreateInfo, Handle pAllocator, Pointer device) {
		return VkResult.SUCCESS;
	}

	@Override
	public void vkDestroyDevice(LogicalDevice device, Handle pAllocator) {
	}

	@Override
	public VkResult vkDeviceWaitIdle(LogicalDevice device) {
		return VkResult.SUCCESS;
	}

	@Override
	public void vkGetDeviceQueue(Handle device, int queueFamilyIndex, int queueIndex, Pointer pQueue) {
	}

	@Override
	public VkResult vkQueueSubmit(WorkQueue queue, int submitCount, VkSubmitInfo[] pSubmits, Fence fence) {
		return VkResult.SUCCESS;
	}

	@Override
	public VkResult vkQueueWaitIdle(WorkQueue queue) {
		return VkResult.SUCCESS;
	}

	@Override
	public int vkCreateCommandPool(LogicalDevice device, VkCommandPoolCreateInfo pCreateInfo, Handle pAllocator, NativeReference<Handle> pCommandPool) {
		return 0;
	}

	@Override
	public void vkDestroyCommandPool(LogicalDevice device, CommandPool commandPool, Handle pAllocator) {
	}

	@Override
	public int vkResetCommandPool(LogicalDevice device, CommandPool commandPool, EnumMask<VkCommandPoolResetFlag> flags) {
		return 0;
	}

	@Override
	public int vkAllocateCommandBuffers(LogicalDevice device, VkCommandBufferAllocateInfo pAllocateInfo, Handle[] pCommandBuffers) {
		return 0;
	}

	@Override
	public void vkFreeCommandBuffers(LogicalDevice device, CommandPool commandPool, int commandBufferCount, CommandBuffer[] pCommandBuffers) {
	}

	@Override
	public int vkBeginCommandBuffer(CommandBuffer commandBuffer, VkCommandBufferBeginInfo pBeginInfo) {
		return 0;
	}

	@Override
	public int vkEndCommandBuffer(CommandBuffer commandBuffer) {
		return 0;
	}

	@Override
	public int vkResetCommandBuffer(CommandBuffer commandBuffer, EnumMask<VkCommandBufferResetFlag> flags) {
		return 0;
	}

	@Override
	public void vkCmdExecuteCommands(CommandBuffer commandBuffer, int commandBufferCount, CommandBuffer[] pCommandBuffers) {
	}

	@Override
	public int vkCreateSemaphore(LogicalDevice device, VkSemaphoreCreateInfo pCreateInfo, Handle pAllocator, Pointer pSemaphore) {
		return 0;
	}

	@Override
	public void vkDestroySemaphore(LogicalDevice device, VulkanSemaphore semaphore, Handle pAllocator) {
	}

	@Override
	public int vkCreateFence(LogicalDevice device, VkFenceCreateInfo pCreateInfo, Handle pAllocator, NativeReference<Handle> pFence) {
		return 0;
	}

	@Override
	public void vkDestroyFence(LogicalDevice device, Fence fence, Handle pAllocator) {
	}

	@Override
	public int vkResetFences(LogicalDevice device, int fenceCount, Fence[] pFences) {
		return 0;
	}

	@Override
	public VkResult vkGetFenceStatus(LogicalDevice device, Fence fence) {
		return null;
	}

	@Override
	public int vkWaitForFences(LogicalDevice device, int fenceCount, Fence[] pFences, boolean waitAll, long timeout) {
		return 0;
	}

	@Override
	public int vkCreateBuffer(LogicalDevice device, VkBufferCreateInfo pCreateInfo, Handle pAllocator, NativeReference<Handle> pBuffer) {
		return 0;
	}

	@Override
	public void vkDestroyBuffer(LogicalDevice device, VulkanBuffer pBuffer, Handle pAllocator) {
	}

	@Override
	public void vkGetBufferMemoryRequirements(LogicalDevice device, Handle pBuffer, VkMemoryRequirements pMemoryRequirements) {
	}

	@Override
	public int vkBindBufferMemory(LogicalDevice device, Handle pBuffer, DeviceMemory memory, long memoryOffset) {
		return 0;
	}

	@Override
	public void vkCmdBindVertexBuffers(CommandBuffer commandBuffer, int firstBinding, int bindingCount, VertexBuffer[] pBuffers, long[] pOffsets) {
	}

	@Override
	public void vkCmdBindIndexBuffer(CommandBuffer commandBuffer, VulkanBuffer buffer, long offset, VkIndexType indexType) {
	}

	@Override
	public void vkCmdCopyBuffer(CommandBuffer commandBuffer, VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, int regionCount, VkBufferCopy[] pRegions) {
	}

	@Override
	public void vkCmdFillBuffer(CommandBuffer commandBuffer, VulkanBuffer dstBuffer, long dstOffset, long size, int data) {
	}

	@Override
	public int vkCreateQueryPool(LogicalDevice device, VkQueryPoolCreateInfo pCreateInfo, Handle pAllocator, NativeReference<Handle> pQueryPool) {
		return 0;
	}

	@Override
	public void vkDestroyQueryPool(LogicalDevice device, Pool queryPool, Handle pAllocator) {
	}

	@Override
	public void vkCmdResetQueryPool(CommandBuffer commandBuffer, Pool queryPool, int firstQuery, int queryCount) {
	}

	@Override
	public void vkCmdBeginQuery(CommandBuffer commandBuffer, Pool queryPool, int query, EnumMask<VkQueryControlFlag> flags) {
	}

	@Override
	public void vkCmdEndQuery(CommandBuffer commandBuffer, Pool queryPool, int query) {
	}

	@Override
	public void vkCmdWriteTimestamp(CommandBuffer commandBuffer, VkPipelineStage pipelineStage, Pool queryPool, int query) {
	}

	@Override
	public int vkGetQueryPoolResults(LogicalDevice device, Pool queryPool, int firstQuery, int queryCount, long dataSize, NativeReference<Handle> pData, long stride, EnumMask<VkQueryResultFlag> flags) {
		return 0;
	}

	@Override
	public void vkCmdCopyQueryPoolResults(CommandBuffer commandBuffer, Pool queryPool, int firstQuery, int queryCount, VulkanBuffer dstBuffer, long dstOffset, long stride, EnumMask<VkQueryResultFlag> flags) {
	}

	@Override
	public int vkAllocateMemory(LogicalDevice device, VkMemoryAllocateInfo pAllocateInfo, Handle pAllocator, NativeReference<Handle> pMemory) {
		return 0;
	}

	@Override
	public void vkFreeMemory(LogicalDevice device, DeviceMemory memory, Handle pAllocator) {
	}

	@Override
	public int vkMapMemory(LogicalDevice device, DeviceMemory memory, long offset, long size, int flags, NativeReference<Handle> ppData) {
		return 0;
	}

	@Override
	public void vkUnmapMemory(LogicalDevice device, DeviceMemory memory) {
	}

	@Override
	public int vkCreateImage(LogicalDevice device, VkImageCreateInfo pCreateInfo, Handle pAllocator, NativeReference<Handle> pImage) {
		return 0;
	}

	@Override
	public void vkDestroyImage(LogicalDevice device, Image image, Handle pAllocator) {
	}

	@Override
	public void vkGetImageMemoryRequirements(LogicalDevice device, Handle image, VkMemoryRequirements pMemoryRequirements) {
	}

	@Override
	public int vkBindImageMemory(LogicalDevice device, Handle image, DeviceMemory memory, long memoryOffset) {
		return 0;
	}

	@Override
	public void vkCmdCopyImage(CommandBuffer commandBuffer, Image srcImage, VkImageLayout srcImageLayout, Image dstImage, VkImageLayout dstImageLayout,
			int regionCount, VkImageCopy[] pRegions) {
	}

	@Override
	public void vkCmdCopyBufferToImage(CommandBuffer commandBuffer, VulkanBuffer srcBuffer, Image dstImage, VkImageLayout dstImageLayout, int regionCount,
			VkBufferImageCopy[] pRegions) {
	}

	@Override
	public void vkCmdCopyImageToBuffer(CommandBuffer commandBuffer, Image srcImage, VkImageLayout srcImageLayout, VulkanBuffer dstBuffer, int regionCount,
			VkBufferImageCopy[] pRegions) {
	}

	@Override
	public void vkCmdBlitImage(CommandBuffer commandBuffer, Image srcImage, VkImageLayout srcImageLayout, Image dstImage, VkImageLayout dstImageLayout,
			int regionCount, VkImageBlit[] pRegions, VkFilter filter) {
	}

	@Override
	public int vkCreateImageView(LogicalDevice device, VkImageViewCreateInfo pCreateInfo, Handle pAllocator, NativeReference<Handle> pView) {
		return 0;
	}

	@Override
	public void vkDestroyImageView(LogicalDevice device, View imageView, Handle pAllocator) {
	}

	@Override
	public void vkCmdClearColorImage(CommandBuffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearColorValue pColor, int rangeCount,
			VkImageSubresourceRange[] pRanges) {
	}

	@Override
	public void vkCmdClearDepthStencilImage(CommandBuffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearDepthStencilValue pDepthStencil,
			int rangeCount, VkImageSubresourceRange[] pRanges) {
	}

	@Override
	public int vkCreateSampler(LogicalDevice device, VkSamplerCreateInfo pCreateInfo, Handle pAllocator, NativeReference<Handle> pSampler) {
		return 0;
	}

	@Override
	public void vkDestroySampler(LogicalDevice device, Sampler sampler, Handle pAllocator) {
	}

	@Override
	public int vkCreateGraphicsPipelines(LogicalDevice device, PipelineCache pipelineCache, int createInfoCount, VkGraphicsPipelineCreateInfo[] pCreateInfos,
			Handle pAllocator, Handle[] pPipelines) {
		return 0;
	}

	@Override
	public int vkCreateComputePipelines(LogicalDevice device, PipelineCache pipelineCache, int createInfoCount, VkComputePipelineCreateInfo[] pCreateInfos,
			Handle pAllocator, Handle[] pPipelines) {
		return 0;
	}

	@Override
	public void vkDestroyPipeline(LogicalDevice device, Pipeline pipeline, Handle pAllocator) {
	}

	@Override
	public void vkCmdBindPipeline(CommandBuffer commandBuffer, VkPipelineBindPoint pipelineBindPoint, Pipeline pipeline) {
	}

	@Override
	public void vkCmdPipelineBarrier(CommandBuffer commandBuffer, EnumMask<VkPipelineStage> srcStageMask, EnumMask<VkPipelineStage> dstStageMask,
			EnumMask<VkDependencyFlag> dependencyFlags, int memoryBarrierCount, VkMemoryBarrier[] pMemoryBarriers, int bufferMemoryBarrierCount,
			VkBufferMemoryBarrier[] pBufferMemoryBarriers, int imageMemoryBarrierCount, VkImageMemoryBarrier[] pImageMemoryBarriers) {
	}

	@Override
	public int vkCreatePipelineLayout(LogicalDevice device, VkPipelineLayoutCreateInfo pCreateInfo, Handle pAllocator,
			NativeReference<Handle> pPipelineLayout) {
		return 0;
	}

	@Override
	public void vkDestroyPipelineLayout(LogicalDevice device, PipelineLayout pipelineLayout, Handle pAllocator) {
	}

	@Override
	public void vkCmdPushConstants(CommandBuffer commandBuffer, PipelineLayout layout, EnumMask<VkShaderStage> stageFlags, int offset, int size,
			Handle pValues) {
	}

	@Override
	public int vkCreatePipelineCache(LogicalDevice device, VkPipelineCacheCreateInfo pCreateInfo, Handle pAllocator, NativeReference<Handle> pPipelineCache) {
		return 0;
	}

	@Override
	public int vkMergePipelineCaches(LogicalDevice device, PipelineCache dstCache, int srcCacheCount, PipelineCache[] pSrcCaches) {
		return 0;
	}

	@Override
	public int vkGetPipelineCacheData(LogicalDevice device, PipelineCache cache, IntegerReference pDataSize, byte[] pData) {
		return 0;
	}

	@Override
	public void vkDestroyPipelineCache(LogicalDevice device, PipelineCache cache, Handle pAllocator) {
	}

	@Override
	public void vkCmdSetViewport(CommandBuffer commandBuffer, int firstViewport, int viewportCount, VkViewport pViewports) {
	}

	@Override
	public void vkCmdSetScissor(CommandBuffer commandBuffer, int firstScissor, int scissorCount, VkRect2D pScissors) {
	}

	@Override
	public void vkCmdSetLineWidth(CommandBuffer commandBuffer, float lineWidth) {
	}

	@Override
	public void vkCmdSetDepthBias(CommandBuffer commandBuffer, float depthBiasConstantFactor, float depthBiasClamp, float depthBiasSlopeFactor) {
	}

	@Override
	public void vkCmdSetBlendConstants(CommandBuffer commandBuffer, float[] blendConstants) {
	}

	@Override
	public void vkCmdSetDepthBounds(CommandBuffer commandBuffer, float minDepthBounds, float maxDepthBounds) {
	}

	@Override
	public void vkCmdSetStencilCompareMask(CommandBuffer commandBuffer, EnumMask<VkStencilFaceFlag> faceMask, int compareMask) {
	}

	@Override
	public void vkCmdSetStencilWriteMask(CommandBuffer commandBuffer, EnumMask<VkStencilFaceFlag> faceMask, int writeMask) {
	}

	@Override
	public void vkCmdSetStencilReference(CommandBuffer commandBuffer, EnumMask<VkStencilFaceFlag> faceMask, int reference) {
	}

	@Override
	public VkResult vkCreateShaderModule(LogicalDevice device, VkShaderModuleCreateInfo info, Handle pAllocator, Pointer shader) {
		return null;
	}

	@Override
	public void vkDestroyShaderModule(LogicalDevice device, Shader shader, Handle pAllocator) {
	}

	@Override
	public VkResult vkGetPhysicalDeviceSurfaceCapabilitiesKHR(PhysicalDevice device, Surface surface, VkSurfaceCapabilitiesKHR pSurfaceCapabilities) {
		return VkResult.SUCCESS;
	}

	@Override
	public VkResult vkGetPhysicalDeviceSurfaceFormatsKHR(PhysicalDevice device, Surface surface, NativeReference<Integer> count, VkSurfaceFormatKHR[] formats) {
		return VkResult.SUCCESS;
	}

	@Override
	public VkResult vkGetPhysicalDeviceSurfacePresentModesKHR(PhysicalDevice device, Surface surface, NativeReference<Integer> count, VkPresentModeKHR[] modes) {
		return VkResult.SUCCESS;
	}

	@Override
	public void vkDestroySurfaceKHR(Instance instance, Surface surface, Handle allocator) {
	}

	@Override
	public VkResult vkCreateSwapchainKHR(LogicalDevice device, VkSwapchainCreateInfoKHR pCreateInfo, Handle pAllocator, Pointer pSwapchain) {
		return VkResult.SUCCESS;
	}

	@Override
	public void vkDestroySwapchainKHR(LogicalDevice device, Swapchain swapchain, Handle pAllocator) {
	}

	@Override
	public VkResult vkGetSwapchainImagesKHR(LogicalDevice device, Handle swapchain, IntegerReference pSwapchainImageCount, Handle[] pSwapchainImages) {
		return VkResult.SUCCESS;
	}

	@Override
	public VkResult vkAcquireNextImageKHR(LogicalDevice device, Swapchain swapchain, long timeout, VulkanSemaphore semaphore, Fence fence, IntegerReference pImageIndex) {
		return VkResult.SUCCESS;
	}

	@Override
	public VkResult vkQueuePresentKHR(WorkQueue queue, VkPresentInfoKHR pPresentInfo) {
		return VkResult.SUCCESS;
	}

	@Override
	public VkResult vkCreateRenderPass(LogicalDevice device, VkRenderPassCreateInfo pCreateInfo, Handle pAllocator, Pointer pRenderPass) {
		return VkResult.SUCCESS;
	}

	@Override
	public void vkDestroyRenderPass(LogicalDevice device, RenderPass renderPass, Handle pAllocator) {
	}

	@Override
	public void vkCmdBeginRenderPass(CommandBuffer commandBuffer, VkRenderPassBeginInfo pRenderPassBegin, VkSubpassContents contents) {
	}

	@Override
	public void vkCmdEndRenderPass(CommandBuffer commandBuffer) {
	}

	@Override
	public void vkCmdNextSubpass(CommandBuffer commandBuffer, VkSubpassContents contents) {
	}

	@Override
	public void vkGetRenderAreaGranularity(LogicalDevice dev, RenderPass renderPass, VkExtent2D pGranularity) {
	}

	@Override
	public void vkCmdClearAttachments(CommandBuffer commandBuffer, int attachmentCount, VkClearAttachment[] pAttachments, int rectCount, VkClearRect[] pRects) {
	}

	@Override
	public VkResult vkCreateFramebuffer(LogicalDevice device, VkFramebufferCreateInfo pCreateInfo, Handle pAllocator, Pointer pFramebuffer) {
		return VkResult.SUCCESS;
	}

	@Override
	public void vkDestroyFramebuffer(LogicalDevice device, FrameBuffer framebuffer, Handle pAllocator) {
	}

	@Override
	public VkResult vkCreateDescriptorSetLayout(LogicalDevice device, VkDescriptorSetLayoutCreateInfo pCreateInfo, Handle pAllocator, NativeReference<Handle> pSetLayout) {
		return VkResult.SUCCESS;
	}

	@Override
	public void vkDestroyDescriptorSetLayout(LogicalDevice device, Layout descriptorSetLayout, Handle pAllocator) {
	}

	@Override
	public VkResult vkCreateDescriptorPool(LogicalDevice device, VkDescriptorPoolCreateInfo pCreateInfo, Handle pAllocator, NativeReference<Handle> pDescriptorPool) {
		return VkResult.SUCCESS;
	}

	@Override
	public void vkDestroyDescriptorPool(LogicalDevice device, org.sarge.jove.platform.vulkan.render.DescriptorSet.Pool descriptorPool, Handle pAllocator) {
	}

	@Override
	public VkResult vkAllocateDescriptorSets(LogicalDevice device, VkDescriptorSetAllocateInfo pAllocateInfo, Handle[] pDescriptorSets) {
		return VkResult.SUCCESS;
	}

	@Override
	public VkResult vkResetDescriptorPool(LogicalDevice device, org.sarge.jove.platform.vulkan.render.DescriptorSet.Pool descriptorPool, int flags) {
		return VkResult.SUCCESS;
	}

	@Override
	public VkResult vkFreeDescriptorSets(LogicalDevice device, org.sarge.jove.platform.vulkan.render.DescriptorSet.Pool descriptorPool, int descriptorSetCount, DescriptorSet[] pDescriptorSets) {
		return VkResult.SUCCESS;
	}

	@Override
	public void vkUpdateDescriptorSets(LogicalDevice device, int descriptorWriteCount, VkWriteDescriptorSet[] pDescriptorWrites, int descriptorCopyCount, VkCopyDescriptorSet[] pDescriptorCopies) {
	}

	@Override
	public void vkCmdBindDescriptorSets(CommandBuffer commandBuffer, VkPipelineBindPoint pipelineBindPoint, PipelineLayout layout, int firstSet, int descriptorSetCount, DescriptorSet[] pDescriptorSets, int dynamicOffsetCount, int[] pDynamicOffsets) {
	}

	@Override
	public void vkCmdDraw(CommandBuffer commandBuffer, int vertexCount, int instanceCount, int firstVertex, int firstInstance) {
	}

	@Override
	public void vkCmdDrawIndexed(CommandBuffer commandBuffer, int indexCount, int instanceCount, int firstIndex, int firstVertex, int firstInstance) {
	}

	@Override
	public void vkCmdDrawIndirect(CommandBuffer commandBuffer, VulkanBuffer buffer, long offset, int drawCount, int stride) {
	}

	@Override
	public void vkCmdDrawIndexedIndirect(CommandBuffer commandBuffer, VulkanBuffer buffer, long offset, int drawCount, int stride) {
	}

	@Override
	public VkResult vkEnumerateInstanceExtensionProperties(String pLayerName, IntegerReference pPropertyCount, VkExtensionProperties[] pProperties) {
		return null;
	}

	@Override
	public VkResult vkEnumerateInstanceLayerProperties(IntegerReference pPropertyCount, VkLayerProperties[] pProperties) {
		return null;
	}

	@Override
	public void vkGetPhysicalDeviceMemoryProperties(PhysicalDevice device, VkPhysicalDeviceMemoryProperties pMemoryProperties) {
	}

	@Override
	public VkResult vkEnumerateDeviceExtensionProperties(PhysicalDevice device, String layer, IntegerReference count, VkExtensionProperties[] extensions) {
		return null;
	}

	@Override
	public VkResult vkEnumerateDeviceLayerProperties(PhysicalDevice device, IntegerReference count, VkLayerProperties[] layers) {
		return null;
	}
}
