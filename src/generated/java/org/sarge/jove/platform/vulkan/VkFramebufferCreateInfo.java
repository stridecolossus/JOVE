package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkFramebufferCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.FRAMEBUFFER_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public Handle renderPass;
	public int attachmentCount;
	public Handle[] pAttachments; // TODO
	public int width;
	public int height;
	public int layers;
}
