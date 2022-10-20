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
	"filterMinmaxSingleComponentFormats",
	"filterMinmaxImageComponentMapping"
})
public class VkPhysicalDeviceSamplerFilterMinmaxPropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceSamplerFilterMinmaxPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceSamplerFilterMinmaxPropertiesEXT implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_SAMPLER_FILTER_MINMAX_PROPERTIES_EXT;
	public Pointer pNext;
	public boolean filterMinmaxSingleComponentFormats;
	public boolean filterMinmaxImageComponentMapping;
}
