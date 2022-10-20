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
	"fragmentDensityMap",
	"fragmentDensityMapDynamic",
	"fragmentDensityMapNonSubsampledImages"
})
public class VkPhysicalDeviceFragmentDensityMapFeaturesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceFragmentDensityMapFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceFragmentDensityMapFeaturesEXT implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_FRAGMENT_DENSITY_MAP_FEATURES_EXT;
	public Pointer pNext;
	public boolean fragmentDensityMap;
	public boolean fragmentDensityMapDynamic;
	public boolean fragmentDensityMapNonSubsampledImages;
}
