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
	"minFragmentDensityTexelSize",
	"maxFragmentDensityTexelSize",
	"fragmentDensityInvocations"
})
public class VkPhysicalDeviceFragmentDensityMapPropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceFragmentDensityMapPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceFragmentDensityMapPropertiesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FRAGMENT_DENSITY_MAP_PROPERTIES_EXT;
	public Pointer pNext;
	public VkExtent2D minFragmentDensityTexelSize;
	public VkExtent2D maxFragmentDensityTexelSize;
	public VulkanBoolean fragmentDensityInvocations;
}
