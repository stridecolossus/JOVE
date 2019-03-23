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
	"fragmentDensityMap",
	"fragmentDensityMapDynamic",
	"fragmentDensityMapNonSubsampledImages"
})
public class VkPhysicalDeviceFragmentDensityMapFeaturesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceFragmentDensityMapFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceFragmentDensityMapFeaturesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FRAGMENT_DENSITY_MAP_FEATURES_EXT.value();
	public Pointer pNext;
	public boolean fragmentDensityMap;
	public boolean fragmentDensityMapDynamic;
	public boolean fragmentDensityMapNonSubsampledImages;
}
