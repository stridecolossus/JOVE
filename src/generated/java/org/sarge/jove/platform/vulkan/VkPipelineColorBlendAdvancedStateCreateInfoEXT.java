package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.*;
import com.sun.jna.Structure.FieldOrder;

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
public class VkPipelineColorBlendAdvancedStateCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkPipelineColorBlendAdvancedStateCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPipelineColorBlendAdvancedStateCreateInfoEXT implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PIPELINE_COLOR_BLEND_ADVANCED_STATE_CREATE_INFO_EXT;
	public Pointer pNext;
	public boolean srcPremultiplied;
	public boolean dstPremultiplied;
	public VkBlendOverlapEXT blendOverlap;
}
