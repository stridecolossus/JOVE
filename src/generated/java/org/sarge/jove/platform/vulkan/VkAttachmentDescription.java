package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
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
public class VkAttachmentDescription extends VulkanStructure {
	public static class ByValue extends VkAttachmentDescription implements Structure.ByValue { }
	public static class ByReference extends VkAttachmentDescription implements Structure.ByReference { }

	public int flags;
	public VkFormat format = VkFormat.VK_FORMAT_UNDEFINED;
	public VkSampleCountFlag samples = VkSampleCountFlag.VK_SAMPLE_COUNT_1_BIT;
	public VkAttachmentLoadOp loadOp = VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_DONT_CARE;
	public VkAttachmentStoreOp storeOp = VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE;
	public VkAttachmentLoadOp stencilLoadOp = VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_DONT_CARE;
	public VkAttachmentStoreOp stencilStoreOp = VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE;
	public VkImageLayout initialLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;
	public VkImageLayout finalLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;
}
