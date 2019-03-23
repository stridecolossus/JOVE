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
	"rasterizationStream"
})
public class VkPipelineRasterizationStateStreamCreateInfoEXT extends Structure {
	public static class ByValue extends VkPipelineRasterizationStateStreamCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPipelineRasterizationStateStreamCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_STREAM_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public int flags;
	public int rasterizationStream;
}
