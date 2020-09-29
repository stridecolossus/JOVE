package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

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
	"srcPremultiplied",
	"dstPremultiplied",
	"blendOverlap"
})
public class VkPipelineColorBlendAdvancedStateCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkPipelineColorBlendAdvancedStateCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPipelineColorBlendAdvancedStateCreateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_ADVANCED_STATE_CREATE_INFO_EXT;
	public Pointer pNext;
	public VulkanBoolean srcPremultiplied;
	public VulkanBoolean dstPremultiplied;
	public VkBlendOverlapEXT blendOverlap;
}
