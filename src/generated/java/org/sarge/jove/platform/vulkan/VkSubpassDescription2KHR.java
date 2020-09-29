package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"pipelineBindPoint",
	"viewMask",
	"inputAttachmentCount",
	"pInputAttachments",
	"colorAttachmentCount",
	"pColorAttachments",
	"pResolveAttachments",
	"pDepthStencilAttachment",
	"preserveAttachmentCount",
	"pPreserveAttachments"
})
public class VkSubpassDescription2KHR extends VulkanStructure {
	public static class ByValue extends VkSubpassDescription2KHR implements Structure.ByValue { }
	public static class ByReference extends VkSubpassDescription2KHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_SUBPASS_DESCRIPTION_2_KHR;
	public Pointer pNext;
	public int flags;
	public VkPipelineBindPoint pipelineBindPoint;
	public int viewMask;
	public int inputAttachmentCount;
	public Pointer pInputAttachments;
	public int colorAttachmentCount;
	public Pointer pColorAttachments;
	public Pointer pResolveAttachments;
	public Pointer pDepthStencilAttachment;
	public int preserveAttachmentCount;
	public Pointer pPreserveAttachments;
}
