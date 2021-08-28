package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

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
	"fragmentDensityMap",
	"fragmentDensityMapDynamic",
	"fragmentDensityMapNonSubsampledImages"
})
public class VkPhysicalDeviceFragmentDensityMapFeaturesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceFragmentDensityMapFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceFragmentDensityMapFeaturesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_FRAGMENT_DENSITY_MAP_FEATURES_EXT;
	public Pointer pNext;
	public VulkanBoolean fragmentDensityMap;
	public VulkanBoolean fragmentDensityMapDynamic;
	public VulkanBoolean fragmentDensityMapNonSubsampledImages;
}
