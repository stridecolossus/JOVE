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
	"subpassCount",
	"pViewMasks",
	"dependencyCount",
	"pViewOffsets",
	"correlationMaskCount",
	"pCorrelationMasks"
})
public class VkRenderPassMultiviewCreateInfo extends Structure {
	public static class ByValue extends VkRenderPassMultiviewCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkRenderPassMultiviewCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_RENDER_PASS_MULTIVIEW_CREATE_INFO.value();
	public Pointer pNext;
	public int subpassCount;
	public int pViewMasks;
	public int dependencyCount;
	public int pViewOffsets;
	public int correlationMaskCount;
	public int pCorrelationMasks;
}
