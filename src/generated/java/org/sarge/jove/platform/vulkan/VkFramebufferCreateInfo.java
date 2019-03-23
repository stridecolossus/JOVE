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
	"renderPass",
	"attachmentCount",
	"pAttachments",
	"width",
	"height",
	"layers"
})
public class VkFramebufferCreateInfo extends VulkanStructure {
	public static class ByValue extends VkFramebufferCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkFramebufferCreateInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public Pointer renderPass;
	public int attachmentCount;
	public Pointer pAttachments;
	public int width;
	public int height;
	public int layers;
}
