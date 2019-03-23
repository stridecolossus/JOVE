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
	"reductionMode"
})
public class VkSamplerReductionModeCreateInfoEXT extends Structure {
	public static class ByValue extends VkSamplerReductionModeCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkSamplerReductionModeCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SAMPLER_REDUCTION_MODE_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public int reductionMode;
}
