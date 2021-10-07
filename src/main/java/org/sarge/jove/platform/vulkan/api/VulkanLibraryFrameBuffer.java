package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.VkClearDepthStencilValue;
import org.sarge.jove.platform.vulkan.VkFramebufferCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkImageSubresourceRange;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.image.Image;
import org.sarge.jove.platform.vulkan.render.FrameBuffer;

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
	int vkCreateFramebuffer(DeviceContext device, VkFramebufferCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pFramebuffer);

	/**
	 * Destroys a frame buffer.
	 * @param device			Logical device
	 * @param framebuffer		Frame buffer
	 * @param pAllocator		Allocator
	 */
	void vkDestroyFramebuffer(DeviceContext device, FrameBuffer framebuffer, Pointer pAllocator);

	void vkCmdClearDepthStencilImage(Command.Buffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearDepthStencilValue pDepthStencil, int rangeCount, VkImageSubresourceRange pRanges);
}
