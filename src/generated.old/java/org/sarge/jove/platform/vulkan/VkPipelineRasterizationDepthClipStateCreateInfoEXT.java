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
	"depthClipEnable"
})
public class VkPipelineRasterizationDepthClipStateCreateInfoEXT extends Structure {
	public static class ByValue extends VkPipelineRasterizationDepthClipStateCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPipelineRasterizationDepthClipStateCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_DEPTH_CLIP_STATE_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public int flags;
	public boolean depthClipEnable;
}
