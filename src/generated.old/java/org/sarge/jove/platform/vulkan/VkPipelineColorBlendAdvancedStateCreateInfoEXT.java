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
	"srcPremultiplied",
	"dstPremultiplied",
	"blendOverlap"
})
public class VkPipelineColorBlendAdvancedStateCreateInfoEXT extends Structure {
	public static class ByValue extends VkPipelineColorBlendAdvancedStateCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPipelineColorBlendAdvancedStateCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_ADVANCED_STATE_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public boolean srcPremultiplied;
	public boolean dstPremultiplied;
	public int blendOverlap;
}
