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
	"reductionMode"
})
public class VkSamplerReductionModeCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkSamplerReductionModeCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkSamplerReductionModeCreateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.SAMPLER_REDUCTION_MODE_CREATE_INFO_EXT;
	public Pointer pNext;
	public VkSamplerReductionModeEXT reductionMode;
}
