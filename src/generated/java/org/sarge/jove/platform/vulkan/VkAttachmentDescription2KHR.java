package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import com.sun.jna.Pointer;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
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
public class VkAttachmentDescription2KHR extends VulkanStructure {
	public static class ByValue extends VkAttachmentDescription2KHR implements Structure.ByValue { }
	public static class ByReference extends VkAttachmentDescription2KHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_ATTACHMENT_DESCRIPTION_2_KHR;
	public Pointer pNext;
	public int flags;
	public VkFormat format;
	public VkSampleCountFlagBits samples;
	public VkAttachmentLoadOp loadOp;
	public VkAttachmentStoreOp storeOp;
	public VkAttachmentLoadOp stencilLoadOp;
	public VkAttachmentStoreOp stencilStoreOp;
	public VkImageLayout initialLayout;
	public VkImageLayout finalLayout;
}
