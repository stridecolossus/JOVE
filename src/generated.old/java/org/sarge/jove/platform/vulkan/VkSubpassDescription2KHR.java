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
	"flags",
	"pipelineBindPoint",
	"viewMask",
	"inputAttachmentCount",
	"pInputAttachments",
	"colorAttachmentCount",
	"pColorAttachments",
	"pResolveAttachments",
	"pDepthStencilAttachment",
	"preserveAttachmentCount",
	"pPreserveAttachments"
})
public class VkSubpassDescription2KHR extends Structure {
	public static class ByValue extends VkSubpassDescription2KHR implements Structure.ByValue { }
	public static class ByReference extends VkSubpassDescription2KHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SUBPASS_DESCRIPTION_2_KHR.value();
	public Pointer pNext;
	public int flags;
	public int pipelineBindPoint;
	public int viewMask;
	public int inputAttachmentCount;
	public VkAttachmentReference2KHR.ByReference pInputAttachments;
	public int colorAttachmentCount;
	public VkAttachmentReference2KHR.ByReference pColorAttachments;
	public VkAttachmentReference2KHR.ByReference pResolveAttachments;
	public VkAttachmentReference2KHR.ByReference pDepthStencilAttachment;
	public int preserveAttachmentCount;
	public int pPreserveAttachments;
}
