package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.platform.vulkan.pipeline.*;
import org.sarge.jove.platform.vulkan.render.*;
import org.sarge.jove.platform.vulkan.render.DescriptorSet.*;
import org.sarge.jove.util.EnumMask;

public class MockVulkanLibrary implements VulkanCoreLibrary, MemoryLibrary, PipelineLibrary, RenderLibrary, ImageLibrary {
	// Instance

	@Override
	public VkResult vkCreateInstance(VkInstanceCreateInfo pCreateInfo, Handle pAllocator, Pointer pInstance) {
		return null;
	}

	@Override
	public void vkDestroyInstance(Instance instance, Handle pAllocator) {
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
	public Handle vkGetInstanceProcAddr(Instance instance, String pName) {
		return null;
	}

	// Physical Device

	@Override
	public VkResult vkEnumeratePhysicalDevices(Instance instance, IntegerReference pPhysicalDeviceCount, Handle[] devices) {
		return null;
	}

	@Override
	public void vkGetPhysicalDeviceProperties(PhysicalDevice device, VkPhysicalDeviceProperties props) {
	}

	@Override
	public void vkGetPhysicalDeviceMemoryProperties(PhysicalDevice device, VkPhysicalDeviceMemoryProperties pMemoryProperties) {
	}

	@Override
	public void vkGetPhysicalDeviceFeatures(Handle device, VkPhysicalDeviceFeatures features) {
	}

	@Override
	public void vkGetPhysicalDeviceQueueFamilyProperties(Handle device, IntegerReference pQueueFamilyPropertyCount, VkQueueFamilyProperties[] pQueueFamilyProperties) {
	}

	@Override
	public VkResult vkEnumerateDeviceExtensionProperties(PhysicalDevice device, String layer, IntegerReference count, VkExtensionProperties[] extensions) {
		return null;
	}

	@Override
	public VkResult vkEnumerateDeviceLayerProperties(PhysicalDevice device, IntegerReference count, VkLayerProperties[] layers) {
		return null;
	}

	@Override
	public void vkGetPhysicalDeviceFormatProperties(PhysicalDevice device, VkFormat format, VkFormatProperties props) {
	}

	// Logical Device

	@Override
	public VkResult vkCreateDevice(PhysicalDevice physicalDevice, VkDeviceCreateInfo pCreateInfo, Handle pAllocator, Pointer device) {
		return null;
	}

	@Override
	public void vkDestroyDevice(LogicalDevice device, Handle pAllocator) {
	}

	@Override
	public VkResult vkDeviceWaitIdle(LogicalDevice device) {
		return null;
	}

	@Override
	public void vkGetDeviceQueue(Handle device, int queueFamilyIndex, int queueIndex, Pointer pQueue) {
	}

	@Override
	public VkResult vkQueueSubmit(WorkQueue queue, int submitCount, VkSubmitInfo[] pSubmits, Fence fence) {
		return null;
	}

	@Override
	public VkResult vkQueueWaitIdle(WorkQueue queue) {
		return null;
	}

	// Swapchain

	@Override
	public VkResult vkCreateSwapchainKHR(LogicalDevice device, VkSwapchainCreateInfoKHR pCreateInfo, Handle pAllocator, Pointer pSwapchain) {
		return null;
	}

	@Override
	public void vkDestroySwapchainKHR(LogicalDevice device, Swapchain swapchain, Handle pAllocator) {
	}

	@Override
	public VkResult vkGetSwapchainImagesKHR(LogicalDevice device, Handle swapchain, IntegerReference pSwapchainImageCount, Handle[] pSwapchainImages) {
		return null;
	}

	@Override
	public int vkAcquireNextImageKHR(LogicalDevice device, Swapchain swapchain, long timeout, VulkanSemaphore semaphore, Fence fence, IntegerReference pImageIndex) {
		return VkResult.SUCCESS.value();
	}

	@Override
	public int vkQueuePresentKHR(WorkQueue queue, VkPresentInfoKHR pPresentInfo) {
		return 0;
	}

    // Fence

	@Override
	public VkResult vkCreateFence(LogicalDevice device, VkFenceCreateInfo pCreateInfo, Handle pAllocator, Pointer pFence) {
		return null;
	}

	@Override
	public void vkDestroyFence(LogicalDevice device, Fence fence, Handle pAllocator) {
	}

	@Override
	public VkResult vkResetFences(LogicalDevice device, int fenceCount, Fence[] pFences) {
		return null;
	}

	@Override
	public int vkGetFenceStatus(LogicalDevice device, Fence fence) {
		return 0;
	}

	@Override
	public VkResult vkWaitForFences(LogicalDevice device, int fenceCount, Fence[] pFences, boolean waitAll, long timeout) {
		return null;
	}

	// Semaphore

	@Override
	public VkResult vkCreateSemaphore(LogicalDevice device, VkSemaphoreCreateInfo pCreateInfo, Handle pAllocator, Pointer pSemaphore) {
		return null;
	}

	@Override
	public void vkDestroySemaphore(LogicalDevice device, VulkanSemaphore semaphore, Handle pAllocator) {
	}

    // Image

	@Override
	public VkResult vkCreateImage(LogicalDevice device, VkImageCreateInfo pCreateInfo, Handle pAllocator, Pointer pImage) {
		return null;
	}

	@Override
	public void vkDestroyImage(LogicalDevice device, Image image, Handle pAllocator) {
	}

	@Override
	public void vkGetImageMemoryRequirements(LogicalDevice device, Handle image, VkMemoryRequirements pMemoryRequirements) {
	}

	@Override
	public VkResult vkBindImageMemory(LogicalDevice device, Handle image, DeviceMemory memory, long memoryOffset) {
		return null;
	}

	@Override
	public void vkCmdCopyImage(Command.Buffer commandBuffer, Image srcImage, VkImageLayout srcImageLayout, Image dstImage, VkImageLayout dstImageLayout, int regionCount, VkImageCopy[] pRegions) {
	}

	@Override
	public void vkCmdCopyBufferToImage(Command.Buffer commandBuffer, VulkanBuffer srcBuffer, Image dstImage, VkImageLayout dstImageLayout, int regionCount, VkBufferImageCopy[] pRegions) {
	}

	@Override
	public void vkCmdCopyImageToBuffer(Command.Buffer commandBuffer, Image srcImage, VkImageLayout srcImageLayout, VulkanBuffer dstBuffer, int regionCount, VkBufferImageCopy[] pRegions) {
	}

	@Override
	public void vkCmdBlitImage(Command.Buffer commandBuffer, Image srcImage, VkImageLayout srcImageLayout, Image dstImage, VkImageLayout dstImageLayout, int regionCount, VkImageBlit[] pRegions, VkFilter filter) {
	}

	// View

    @Override
    public VkResult vkCreateImageView(LogicalDevice device, VkImageViewCreateInfo pCreateInfo, Handle pAllocator, Pointer pView) {
    	return null;
    }

    @Override
    public void vkDestroyImageView(LogicalDevice device, View imageView, Handle pAllocator) {
    }

    @Override
    public void vkCmdClearColorImage(Command.Buffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearColorValue pColor, int rangeCount, VkImageSubresourceRange[] pRanges) {
    }

    @Override
    public void vkCmdClearDepthStencilImage(Command.Buffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearDepthStencilValue pDepthStencil, int rangeCount, VkImageSubresourceRange[] pRanges) {
    }

    // Command

	@Override
	public VkResult vkCreateCommandPool(LogicalDevice device, VkCommandPoolCreateInfo pCreateInfo, Handle pAllocator, Pointer pCommandPool) {
		return null;
	}

	@Override
	public void vkDestroyCommandPool(LogicalDevice device, Command.Pool commandPool, Handle pAllocator) {
	}

	@Override
	public VkResult vkResetCommandPool(LogicalDevice device, Command.Pool commandPool, EnumMask<VkCommandPoolResetFlag> flags) {
		return null;
	}

	@Override
	public VkResult vkAllocateCommandBuffers(LogicalDevice device, VkCommandBufferAllocateInfo pAllocateInfo, Handle[] pCommandBuffers) {
		return null;
	}

	@Override
	public void vkFreeCommandBuffers(LogicalDevice device, Command.Pool commandPool, int commandBufferCount, Command.Buffer[] pCommandBuffers) {
	}

	@Override
	public VkResult vkBeginCommandBuffer(Command.Buffer commandBuffer, VkCommandBufferBeginInfo pBeginInfo) {
		return null;
	}

	@Override
	public VkResult vkEndCommandBuffer(Command.Buffer commandBuffer) {
		return null;
	}

	@Override
	public VkResult vkResetCommandBuffer(Command.Buffer commandBuffer, EnumMask<VkCommandBufferResetFlag> flags) {
		return null;
	}

	@Override
	public void vkCmdExecuteCommands(Command.Buffer commandBuffer, int commandBufferCount, Command.Buffer[] pCommandBuffers) {
	}

	// Buffer

	@Override
	public VkResult vkCreateBuffer(LogicalDevice device, VkBufferCreateInfo pCreateInfo, Handle pAllocator, Pointer pBuffer) {
		return null;
	}

	@Override
	public void vkDestroyBuffer(LogicalDevice device, VulkanBuffer pBuffer, Handle pAllocator) {
	}

	@Override
	public void vkGetBufferMemoryRequirements(LogicalDevice device, Handle pBuffer, VkMemoryRequirements pMemoryRequirements) {
	}

	@Override
	public VkResult vkBindBufferMemory(LogicalDevice device, Handle pBuffer, DeviceMemory memory, long memoryOffset) {
		return null;
	}

	@Override
	public void vkCmdBindVertexBuffers(Buffer commandBuffer, int firstBinding, int bindingCount, VulkanBuffer[] pBuffers, long[] pOffsets) {
	}

	@Override
	public void vkCmdBindIndexBuffer(Buffer commandBuffer, VulkanBuffer buffer, long offset, VkIndexType indexType) {
	}

	@Override
	public void vkCmdCopyBuffer(Buffer commandBuffer, VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, int regionCount, VkBufferCopy[] pRegions) {
	}

	@Override
	public void vkCmdFillBuffer(Buffer commandBuffer, VulkanBuffer dstBuffer, long dstOffset, long size, int data) {
	}

	// Memory

	@Override
	public VkResult vkAllocateMemory(LogicalDevice device, VkMemoryAllocateInfo pAllocateInfo, Handle pAllocator, Pointer pMemory) {
		return null;
	}

	@Override
	public void vkFreeMemory(LogicalDevice device, DeviceMemory memory, Handle pAllocator) {
	}

	@Override
	public VkResult vkMapMemory(LogicalDevice device, DeviceMemory memory, long offset, long size, int flags, Pointer ppData) {
		return null;
	}

	@Override
	public void vkUnmapMemory(LogicalDevice device, DeviceMemory memory) {
	}

	// Vulkan surface

	@Override
	public VkResult vkGetPhysicalDeviceSurfaceSupportKHR(PhysicalDevice device, int queueFamilyIndex, VulkanSurface surface, IntegerReference supported) {
		return null;
	}

	@Override
	public VkResult vkGetPhysicalDeviceSurfaceCapabilitiesKHR(PhysicalDevice device, VulkanSurface surface, VkSurfaceCapabilitiesKHR pSurfaceCapabilities) {
		return null;
	}

	@Override
	public VkResult vkGetPhysicalDeviceSurfaceFormatsKHR(PhysicalDevice device, VulkanSurface surface, IntegerReference count, VkSurfaceFormatKHR[] formats) {
		return null;
	}

	@Override
	public VkResult vkGetPhysicalDeviceSurfacePresentModesKHR(PhysicalDevice device, VulkanSurface surface, IntegerReference count, VkPresentModeKHR[] modes) {
		return null;
	}

	@Override
	public void vkDestroySurfaceKHR(Instance instance, VulkanSurface surface, Handle allocator) {
	}

	// Sampler

	@Override
	public VkResult vkCreateSampler(LogicalDevice device, VkSamplerCreateInfo pCreateInfo, Handle pAllocator, Pointer pSampler) {
		return null;
	}

	@Override
	public void vkDestroySampler(LogicalDevice device, Sampler sampler, Handle pAllocator) {
	}

	// Shader

	@Override
	public VkResult vkCreateShaderModule(LogicalDevice device, VkShaderModuleCreateInfo info, Handle pAllocator, Pointer shader) {
		return null;
	}

	@Override
	public void vkDestroyShaderModule(LogicalDevice device, Shader shader, Handle pAllocator) {
	}

	@Override
	public VkResult vkCreateRenderPass(LogicalDevice device, VkRenderPassCreateInfo pCreateInfo, Handle pAllocator, Pointer pRenderPass) {
		return null;
	}

	// Render Pass

	@Override
	public void vkDestroyRenderPass(LogicalDevice device, RenderPass renderPass, Handle pAllocator) {
	}

	@Override
	public void vkCmdNextSubpass(Buffer commandBuffer, VkSubpassContents contents) {
	}

	@Override
	public void vkGetRenderAreaGranularity(LogicalDevice dev, RenderPass renderPass, VkExtent2D pGranularity) {
	}

	@Override
	public void vkCmdClearAttachments(Buffer commandBuffer, int attachmentCount, VkClearAttachment[] pAttachments, int rectCount, VkClearRect[] pRects) {
	}

	// Frame buffer

	@Override
	public VkResult vkCreateFramebuffer(LogicalDevice device, VkFramebufferCreateInfo pCreateInfo, Handle pAllocator, Pointer pFramebuffer) {
		return null;
	}

	@Override
	public void vkDestroyFramebuffer(LogicalDevice device, Framebuffer framebuffer, Handle pAllocator) {
	}

	@Override
	public void vkCmdBeginRenderPass(Buffer commandBuffer, VkRenderPassBeginInfo pRenderPassBegin, VkSubpassContents contents) {
	}

	@Override
	public void vkCmdEndRenderPass(Buffer commandBuffer) {
	}

	// Descriptor Sets

	@Override
	public VkResult vkCreateDescriptorSetLayout(LogicalDevice device, VkDescriptorSetLayoutCreateInfo pCreateInfo, Handle pAllocator, Pointer pSetLayout) {
		return null;
	}

	@Override
	public void vkDestroyDescriptorSetLayout(LogicalDevice device, Layout descriptorSetLayout, Handle pAllocator) {
	}

	@Override
	public VkResult vkCreateDescriptorPool(LogicalDevice device, VkDescriptorPoolCreateInfo pCreateInfo, Handle pAllocator, Pointer pDescriptorPool) {
		return null;
	}

	@Override
	public void vkDestroyDescriptorPool(LogicalDevice device, Pool descriptorPool, Handle pAllocator) {
	}

	@Override
	public VkResult vkAllocateDescriptorSets(LogicalDevice device, VkDescriptorSetAllocateInfo pAllocateInfo, Handle[] pDescriptorSets) {
		return null;
	}

	@Override
	public VkResult vkResetDescriptorPool(LogicalDevice device, Pool descriptorPool, int flags) {
		return null;
	}

	@Override
	public VkResult vkFreeDescriptorSets(LogicalDevice device, Pool descriptorPool, int descriptorSetCount, DescriptorSet[] pDescriptorSets) {
		return null;
	}

	@Override
	public void vkUpdateDescriptorSets(LogicalDevice device, int descriptorWriteCount, VkWriteDescriptorSet[] pDescriptorWrites, int descriptorCopyCount, VkCopyDescriptorSet[] pDescriptorCopies) {
	}

	@Override
	public void vkCmdBindDescriptorSets(Buffer commandBuffer, VkPipelineBindPoint pipelineBindPoint, PipelineLayout layout, int firstSet, int descriptorSetCount, DescriptorSet[] pDescriptorSets, int dynamicOffsetCount, int[] pDynamicOffsets) {
	}

	// Draw Commands

	@Override
	public void vkCmdDraw(Buffer commandBuffer, int vertexCount, int instanceCount, int firstVertex, int firstInstance) {
	}

	@Override
	public void vkCmdDrawIndexed(Buffer commandBuffer, int indexCount, int instanceCount, int firstIndex, int firstVertex, int firstInstance) {
	}

	@Override
	public void vkCmdDrawIndirect(Buffer commandBuffer, VulkanBuffer buffer, long offset, int drawCount, int stride) {
	}

	@Override
	public void vkCmdDrawIndexedIndirect(Buffer commandBuffer, VulkanBuffer buffer, long offset, int drawCount, int stride) {
	}

	// Pipeline layout

	@Override
	public VkResult vkCreatePipelineLayout(LogicalDevice device, VkPipelineLayoutCreateInfo pCreateInfo, Handle pAllocator, Pointer pPipelineLayout) {
		return null;
	}

	@Override
	public void vkDestroyPipelineLayout(LogicalDevice device, PipelineLayout pipelineLayout, Handle pAllocator) {
	}

	@Override
	public void vkCmdPushConstants(Buffer commandBuffer, PipelineLayout layout, EnumMask<VkShaderStage> stageFlags, int offset, int size, Handle pValues) {
	}

	// Pipeline

	@Override
	public VkResult vkCreateGraphicsPipelines(LogicalDevice device, PipelineCache pipelineCache, int createInfoCount, VkGraphicsPipelineCreateInfo[] pCreateInfos, Handle pAllocator, Handle[] pPipelines) {
		return null;
	}

//	@Override
//	public VkResult vkCreateComputePipelines(LogicalDevice device, PipelineCache pipelineCache, int createInfoCount, VkComputePipelineCreateInfo[] pCreateInfos, Handle pAllocator, Handle[] pPipelines) {
//		return null;
//	}

	@Override
	public void vkDestroyPipeline(LogicalDevice device, Pipeline pipeline, Handle pAllocator) {
	}

	@Override
	public void vkCmdBindPipeline(Buffer commandBuffer, VkPipelineBindPoint pipelineBindPoint, Pipeline pipeline) {
	}

	@Override
	public void vkCmdPipelineBarrier(Buffer commandBuffer, EnumMask<VkPipelineStage> srcStageMask, EnumMask<VkPipelineStage> dstStageMask, EnumMask<VkDependencyFlag> dependencyFlags, int memoryBarrierCount, VkMemoryBarrier[] pMemoryBarriers, int bufferMemoryBarrierCount, VkBufferMemoryBarrier[] pBufferMemoryBarriers, int imageMemoryBarrierCount, VkImageMemoryBarrier[] pImageMemoryBarriers) {
	}
}
