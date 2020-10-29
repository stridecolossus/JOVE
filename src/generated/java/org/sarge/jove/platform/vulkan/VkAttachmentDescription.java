package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure.ByReference;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"flags",
	"format",
	"samples",
	"loadOp",
	"storeOp",
	"stencilLoadOp",
	"stencilStoreOp",
	"initialLayout",
	"finalLayout"
})
public class VkAttachmentDescription extends VulkanStructure implements ByReference {
	public int flags;
	public VkFormat format;
	public VkSampleCountFlag samples;
	public VkAttachmentLoadOp loadOp;
	public VkAttachmentStoreOp storeOp;
	public VkAttachmentLoadOp stencilLoadOp;
	public VkAttachmentStoreOp stencilStoreOp;
	public VkImageLayout initialLayout;
	public VkImageLayout finalLayout;
}
