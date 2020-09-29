package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.VkFramebufferCreateInfo;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

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