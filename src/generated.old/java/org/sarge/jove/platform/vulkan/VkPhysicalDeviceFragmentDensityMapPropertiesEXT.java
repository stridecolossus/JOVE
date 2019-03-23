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
	"minFragmentDensityTexelSize",
	"maxFragmentDensityTexelSize",
	"fragmentDensityInvocations"
})
public class VkPhysicalDeviceFragmentDensityMapPropertiesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceFragmentDensityMapPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceFragmentDensityMapPropertiesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FRAGMENT_DENSITY_MAP_PROPERTIES_EXT.value();
	public Pointer pNext;
	public VkExtent2D minFragmentDensityTexelSize;
	public VkExtent2D maxFragmentDensityTexelSize;
	public boolean fragmentDensityInvocations;
}
