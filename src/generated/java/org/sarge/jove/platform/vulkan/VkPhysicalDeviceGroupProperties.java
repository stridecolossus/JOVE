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
	"physicalDeviceCount",
	"physicalDevices",
	"subsetAllocation"
})
public class VkPhysicalDeviceGroupProperties extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceGroupProperties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceGroupProperties implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_GROUP_PROPERTIES;
	public Pointer pNext;
	public int physicalDeviceCount;
	public Pointer[] physicalDevices = new Pointer[32];
	public boolean subsetAllocation;
}
