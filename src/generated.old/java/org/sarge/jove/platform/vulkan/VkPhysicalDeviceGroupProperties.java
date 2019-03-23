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
	"physicalDeviceCount",
	"physicalDevices",
	"subsetAllocation"
})
public class VkPhysicalDeviceGroupProperties extends Structure {
	public static class ByValue extends VkPhysicalDeviceGroupProperties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceGroupProperties implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_GROUP_PROPERTIES.value();
	public Pointer pNext;
	public int physicalDeviceCount;
	public final Pointer[] physicalDevices = new Pointer[32];
	public boolean subsetAllocation;
}
