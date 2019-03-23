package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

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
public class VkRenderPassInputAttachmentAspectCreateInfo extends Structure {
	public static class ByValue extends VkRenderPassInputAttachmentAspectCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkRenderPassInputAttachmentAspectCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_RENDER_PASS_INPUT_ATTACHMENT_ASPECT_CREATE_INFO.value();
	public Pointer pNext;
	public int aspectReferenceCount;
	public VkInputAttachmentAspectReference.ByReference pAspectReferences;
}
