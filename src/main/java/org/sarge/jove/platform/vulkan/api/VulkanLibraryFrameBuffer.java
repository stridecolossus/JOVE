package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkFramebufferCreateInfo;

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
	int vkCreateFramebuffer(Handle device, VkFramebufferCreateInfo pCreateInfo, Handle pAllocator, PointerByReference pFramebuffer);

	/**
	 * Destroys a frame buffer.
	 * @param device			Logical device
	 * @param framebuffer		Frame buffer
	 * @param pAllocator		Allocator
	 */
	void vkDestroyFramebuffer(Handle device, Handle framebuffer, Handle pAllocator);
}
