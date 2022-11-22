package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.*;

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
public class VkSubpassDescription extends VulkanStructure implements ByReference {
	public BitMask<VkSubpassDescriptionFlag> flags;
	public VkPipelineBindPoint pipelineBindPoint;
	public int inputAttachmentCount;
	public Pointer pInputAttachments;
	public int colorAttachmentCount;
	public VkAttachmentReference pColorAttachments;
	public Pointer pResolveAttachments;
	public VkAttachmentReference pDepthStencilAttachment;
	public int preserveAttachmentCount;
	public Pointer pPreserveAttachments;
}
