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
	"attachmentCount",
	"pAttachments",
	"subpassCount",
	"pSubpasses",
	"dependencyCount",
	"pDependencies",
	"correlatedViewMaskCount",
	"pCorrelatedViewMasks"
})
public class VkRenderPassCreateInfo2KHR extends Structure {
	public static class ByValue extends VkRenderPassCreateInfo2KHR implements Structure.ByValue { }
	public static class ByReference extends VkRenderPassCreateInfo2KHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO_2_KHR.value();
	public Pointer pNext;
	public int flags;
	public int attachmentCount;
	public VkAttachmentDescription2KHR.ByReference pAttachments;
	public int subpassCount;
	public VkSubpassDescription2KHR.ByReference pSubpasses;
	public int dependencyCount;
	public VkSubpassDependency2KHR.ByReference pDependencies;
	public int correlatedViewMaskCount;
	public int pCorrelatedViewMasks;
}
