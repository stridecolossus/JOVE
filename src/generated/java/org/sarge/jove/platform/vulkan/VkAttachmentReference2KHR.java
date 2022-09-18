package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
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
	public VkStructureType sType = VkStructureType.ATTACHMENT_REFERENCE_2_KHR;
	public Pointer pNext;
	public int attachment;
	public VkImageLayout layout;
	public VkImageAspect aspectMask;
}
