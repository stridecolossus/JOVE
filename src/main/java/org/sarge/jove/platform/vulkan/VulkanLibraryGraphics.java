package org.sarge.jove.platform.vulkan;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan graphics API.
 * @author Sarge
 */
interface VulkanLibraryGraphics extends VulkanLibraryImage, VulkanLibrarySurface, VulkanLibrarySwapChain, VulkanLibraryPipeline, VulkanLibraryRenderPass, VulkanLibraryFrameBuffer, VulkanLibraryDescriptorSet {
	// Aggregate interface
}

/**
 * Image and views API.
 */
interface VulkanLibraryImage {
	/**
	 * Creates an image.
	 * @param device			Logical device
	 * @param pCreateInfo		Descriptor
	 * @param pAllocator		Allocator
	 * @param pImage			Returned image
	 * @return Result code
	 */
	int vkCreateImage(Pointer device, VkImageCreateInfo pCreateInfo, Pointer pAllocator, Pointer pImage);

	/**
	 * Destroys an image.
	 * @param device			Logical device
	 * @param image				Image
	 * @param pAllocator		Allocator
	 */
	void vkDestroyImage(Pointer device, Pointer image, Pointer pAllocator);

	/**
	 * Creates an image view.
	 * @param device			Logical device
	 * @param pCreateInfo		Image view descriptor
	 * @param pAllocator		Allocator
	 * @param pView				Returned image view handle
	 * @return Result code
	 */
	int vkCreateImageView(Pointer device, VkImageViewCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pView);

	/**
	 * Destroys an image view.
	 * @param device			Logical device
	 * @param imageView			Image view
	 * @param pAllocator		Allocator
	 */
	void vkDestroyImageView(Pointer device, Pointer imageView, Pointer pAllocator);
}

/**
 * Vulkan surface API.
 */
interface VulkanLibrarySurface {
	/**
	 * Queries whether a queue family supports presentation for the given surface.
	 * @param device				Physical device handle
	 * @param queueFamilyIndex		Queue family
	 * @param surface				Vulkan surface
	 * @param supported				Returned boolean flag
	 * @return Result
	 */
	int vkGetPhysicalDeviceSurfaceSupportKHR(Pointer device, int queueFamilyIndex, Pointer surface, IntByReference supported);

	/**
	 * Retrieves the capabilities of a surface.
	 * @param device			Physical device
	 * @param surface			Surface handle
	 * @param caps				Returned capabilities
	 * @return Result
	 */
	int vkGetPhysicalDeviceSurfaceCapabilitiesKHR(Pointer device, Pointer surface, VkSurfaceCapabilitiesKHR caps);

	/**
	 * Queries the supported surface formats.
	 * @param device			Physical device
	 * @param surface			Surface
	 * @param count				Number of results
	 * @param formats			Supported formats
	 * @return Result
	 */
	int vkGetPhysicalDeviceSurfaceFormatsKHR(Pointer device, Pointer surface, IntByReference count, VkSurfaceFormatKHR formats);

	/**
	 * Queries the supported presentation modes.
	 * @param device			Physical device
	 * @param surface			Surface
	 * @param count				Number of results
	 * @param modes				Supported presentation modes
	 * @return Result
	 * @see VkPresentModeKHR
	 */
	int vkGetPhysicalDeviceSurfacePresentModesKHR(Pointer device, Pointer surface, IntByReference count, int[] modes); // PointerByReference modes);

	/**
	 * Destroys a surface.
	 * @param instance			Vulkan instance
	 * @param surface			Surface
	 * @param allocator			Allocator
	 */
	void vkDestroySurfaceKHR(Pointer instance, Pointer surface, Pointer allocator);
}

/**
 * Swap-chain API.
 */
interface VulkanLibrarySwapChain {
	/**
	 * Creates a swap-chain for the given device.
	 * @param device			Logical device
	 * @param pCreateInfo		Swap-chain descriptor
	 * @param pAllocator		Allocator
	 * @param pSwapchain		Returned swap-chain handle
	 * @return Result code
	 */
	int vkCreateSwapchainKHR(Pointer device, VkSwapchainCreateInfoKHR pCreateInfo, Pointer pAllocator, PointerByReference pSwapchain);

	/**
	 * Destroys a swap-chain.
	 * @param device			Logical device
	 * @param swapchain			Swap-chain
	 * @param pAllocator		Allocator
	 */
	void vkDestroySwapchainKHR(Pointer device, Pointer swapchain, Pointer pAllocator);

	/**
	 * Retrieves swap-chain image handles.
	 * @param device					Logical device
	 * @param swapchain					Swap-chain handle
	 * @param pSwapchainImageCount		Number of images
	 * @param pSwapchainImages			Image handles
	 * @return Result code
	 */
	int vkGetSwapchainImagesKHR(Pointer device, Pointer swapchain, IntByReference pSwapchainImageCount, Pointer[] pSwapchainImages);

	/**
	 * Acquires the next image in the swap-chain.
	 * @param device				Logical device
	 * @param swapchain				Swap-chain
	 * @param timeout				Timeout (ns) or {@link Long#MAX_VALUE} to disable
	 * @param semaphore				Optional semaphore
	 * @param fence					Optional fence
	 * @param pImageIndex			Returned image index
	 * @return Result code
	 */
	int vkAcquireNextImageKHR(Pointer device, Pointer swapchain, long timeout, Pointer semaphore, Pointer fence, IntByReference pImageIndex);
}

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
}

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
}

/**
 * Frame buffer API.
 */
interface VulkanLibraryFrameBuffer {
	/**
	 * Creates a frame buffer.
	 * @param device			Logical device
	 * @param pCreateInfo		Descriptor
	 * @param pAllocator		Allocator
	 * @param pFramebuffer		Returned frame buffer
	 * @return Result code
	 */
	int vkCreateFramebuffer(Pointer device, VkFramebufferCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pFramebuffer);

	/**
	 * Destroys a frame buffer.
	 * @param device			Logical device
	 * @param framebuffer		Frame buffer
	 * @param pAllocator		Allocator
	 */
	void vkDestroyFramebuffer(Pointer device, Pointer framebuffer, Pointer pAllocator);
}

/**
 * Descriptor sets API.
 */
interface VulkanLibraryDescriptorSet {
	int vkCreateDescriptorSetLayout(Pointer device, VkDescriptorSetLayoutCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pSetLayout);
	void vkDestroyDescriptorSetLayout(Pointer device, Pointer descriptorSetLayout, Pointer pAllocator);

	int vkCreateDescriptorPool(Pointer device, VkDescriptorPoolCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pDescriptorPool);
	void vkDestroyDescriptorPool(Pointer device, Pointer descriptorPool, Pointer pAllocator);
	int vkResetDescriptorPool(Pointer device, Pointer descriptorPool, int flags);

	int vkAllocateDescriptorSets(Pointer device, VkDescriptorSetAllocateInfo pAllocateInfo, Pointer[] pDescriptorSets);
	int vkFreeDescriptorSets(Pointer device, Pointer descriptorPool, int descriptorSetCount, Pointer[] pDescriptorSets);

	void vkUpdateDescriptorSets(Pointer device, int descriptorWriteCount, VkWriteDescriptorSet[] pDescriptorWrites, int descriptorCopyCount, VkCopyDescriptorSet[] pDescriptorCopies);
	void vkCmdBindDescriptorSets(Pointer commandBuffer, VkPipelineBindPoint pipelineBindPoint, Pointer layout, int firstSet, int descriptorSetCount, Pointer[] pDescriptorSets, int dynamicOffsetCount, int[] pDynamicOffsets);
}
