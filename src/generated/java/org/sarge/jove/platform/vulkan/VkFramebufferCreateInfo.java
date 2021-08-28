package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"renderPass",
	"attachmentCount",
	"pAttachments",
	"width",
	"height",
	"layers"
})
public class VkFramebufferCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.FRAMEBUFFER_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public Handle renderPass;
	public int attachmentCount;
	public Handle pAttachments;
	public int width;
	public int height;
	public int layers;
}
