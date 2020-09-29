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
	"filterMinmaxSingleComponentFormats",
	"filterMinmaxImageComponentMapping"
})
public class VkPhysicalDeviceSamplerFilterMinmaxPropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceSamplerFilterMinmaxPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceSamplerFilterMinmaxPropertiesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SAMPLER_FILTER_MINMAX_PROPERTIES_EXT;
	public Pointer pNext;
	public VulkanBoolean filterMinmaxSingleComponentFormats;
	public VulkanBoolean filterMinmaxImageComponentMapping;
}
