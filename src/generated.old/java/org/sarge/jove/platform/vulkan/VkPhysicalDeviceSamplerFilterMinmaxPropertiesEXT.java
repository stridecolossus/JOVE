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
	"filterMinmaxSingleComponentFormats",
	"filterMinmaxImageComponentMapping"
})
public class VkPhysicalDeviceSamplerFilterMinmaxPropertiesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceSamplerFilterMinmaxPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceSamplerFilterMinmaxPropertiesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SAMPLER_FILTER_MINMAX_PROPERTIES_EXT.value();
	public Pointer pNext;
	public boolean filterMinmaxSingleComponentFormats;
	public boolean filterMinmaxImageComponentMapping;
}
