package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"flags",
	"pipelineBindPoint",
	"inputAttachmentCount",
	"pInputAttachments",
	"colorAttachmentCount",
	"pColorAttachments",
	"pResolveAttachments",
	"pDepthStencilAttachment",
	"preserveAttachmentCount",
	"pPreserveAttachments"
})
public class VkSubpassDescription extends VulkanStructure {
	public int flags;
	public VkPipelineBindPoint pipelineBindPoint;
	public int inputAttachmentCount;
	public Pointer pInputAttachments;
	public int colorAttachmentCount;
	public Pointer pColorAttachments;
	public Pointer pResolveAttachments;
	public VkAttachmentReference.ByReference pDepthStencilAttachment;
	public int preserveAttachmentCount;
	public Pointer pPreserveAttachments;
}
