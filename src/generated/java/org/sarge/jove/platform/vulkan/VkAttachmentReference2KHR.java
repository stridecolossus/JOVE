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
	"attachment",
	"layout",
	"aspectMask"
})
public class VkAttachmentReference2KHR extends VulkanStructure {
	public static class ByValue extends VkAttachmentReference2KHR implements Structure.ByValue { }
	public static class ByReference extends VkAttachmentReference2KHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_ATTACHMENT_REFERENCE_2_KHR;
	public Pointer pNext;
	public int attachment;
	public VkImageLayout layout;
	public VkImageAspectFlags aspectMask;
}
