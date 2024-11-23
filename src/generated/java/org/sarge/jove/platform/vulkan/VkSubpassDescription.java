package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkSubpassDescription extends VulkanStructure {
	public BitMask<VkSubpassDescriptionFlag> flags;
	public VkPipelineBindPoint pipelineBindPoint;
	public int inputAttachmentCount;
	public Handle[] pInputAttachments;
	public int colorAttachmentCount;
	public VkAttachmentReference pColorAttachments;
	public Handle[] pResolveAttachments;
	public VkAttachmentReference pDepthStencilAttachment;
	public int preserveAttachmentCount;
	public Handle[] pPreserveAttachments;
}
