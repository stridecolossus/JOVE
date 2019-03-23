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
	"samplerYcbcrConversion"
})
public class VkPhysicalDeviceSamplerYcbcrConversionFeatures extends Structure {
	public static class ByValue extends VkPhysicalDeviceSamplerYcbcrConversionFeatures implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceSamplerYcbcrConversionFeatures implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SAMPLER_YCBCR_CONVERSION_FEATURES.value();
	public Pointer pNext;
	public boolean samplerYcbcrConversion;
}
