package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"attachmentCount",
	"pAttachments",
	"subpassCount",
	"pSubpasses",
	"dependencyCount",
	"pDependencies",
	"correlatedViewMaskCount",
	"pCorrelatedViewMasks"
})
public class VkRenderPassCreateInfo2KHR extends VulkanStructure {
	public static class ByValue extends VkRenderPassCreateInfo2KHR implements Structure.ByValue { }
	public static class ByReference extends VkRenderPassCreateInfo2KHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.RENDER_PASS_CREATE_INFO_2_KHR;
	public Pointer pNext;
	public int flags;
	public int attachmentCount;
	public Pointer pAttachments;
	public int subpassCount;
	public Pointer pSubpasses;
	public int dependencyCount;
	public Pointer pDependencies;
	public int correlatedViewMaskCount;
	public Pointer pCorrelatedViewMasks;
}
