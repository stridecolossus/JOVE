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
	"exclusiveScissorCount",
	"pExclusiveScissors"
})
public class VkPipelineViewportExclusiveScissorStateCreateInfoNV extends Structure {
	public static class ByValue extends VkPipelineViewportExclusiveScissorStateCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkPipelineViewportExclusiveScissorStateCreateInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_EXCLUSIVE_SCISSOR_STATE_CREATE_INFO_NV.value();
	public Pointer pNext;
	public int exclusiveScissorCount;
	public VkRect2D.ByReference pExclusiveScissors;
}
