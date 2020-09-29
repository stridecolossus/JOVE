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
	"aspectReferenceCount",
	"pAspectReferences"
})
public class VkRenderPassInputAttachmentAspectCreateInfo extends VulkanStructure {
	public static class ByValue extends VkRenderPassInputAttachmentAspectCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkRenderPassInputAttachmentAspectCreateInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_RENDER_PASS_INPUT_ATTACHMENT_ASPECT_CREATE_INFO;
	public Pointer pNext;
	public int aspectReferenceCount;
	public Pointer pAspectReferences;
}
