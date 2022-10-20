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
	"samplerYcbcrConversion"
})
public class VkPhysicalDeviceSamplerYcbcrConversionFeatures extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceSamplerYcbcrConversionFeatures implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceSamplerYcbcrConversionFeatures implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_SAMPLER_YCBCR_CONVERSION_FEATURES;
	public Pointer pNext;
	public boolean samplerYcbcrConversion;
}
