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
	"viewportWScalingEnable",
	"viewportCount",
	"pViewportWScalings"
})
public class VkPipelineViewportWScalingStateCreateInfoNV extends Structure {
	public static class ByValue extends VkPipelineViewportWScalingStateCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkPipelineViewportWScalingStateCreateInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_W_SCALING_STATE_CREATE_INFO_NV.value();
	public Pointer pNext;
	public boolean viewportWScalingEnable;
	public int viewportCount;
	public VkViewportWScalingNV.ByReference pViewportWScalings;
}
