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
	"depthResolveMode",
	"stencilResolveMode",
	"pDepthStencilResolveAttachment"
})
public class VkSubpassDescriptionDepthStencilResolveKHR extends Structure {
	public static class ByValue extends VkSubpassDescriptionDepthStencilResolveKHR implements Structure.ByValue { }
	public static class ByReference extends VkSubpassDescriptionDepthStencilResolveKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SUBPASS_DESCRIPTION_DEPTH_STENCIL_RESOLVE_KHR.value();
	public Pointer pNext;
	public int depthResolveMode;
	public int stencilResolveMode;
	public VkAttachmentReference2KHR.ByReference pDepthStencilResolveAttachment;
}
