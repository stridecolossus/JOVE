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
	"subpassCount",
	"pViewMasks",
	"dependencyCount",
	"pViewOffsets",
	"correlationMaskCount",
	"pCorrelationMasks"
})
public class VkRenderPassMultiviewCreateInfo extends VulkanStructure {
	public static class ByValue extends VkRenderPassMultiviewCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkRenderPassMultiviewCreateInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.RENDER_PASS_MULTIVIEW_CREATE_INFO;
	public Pointer pNext;
	public int subpassCount;
	public Pointer pViewMasks;
	public int dependencyCount;
	public Pointer pViewOffsets;
	public int correlationMaskCount;
	public Pointer pCorrelationMasks;
}
