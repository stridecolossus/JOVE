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
	"pPhysicalDevices"
})
public class VkDeviceGroupDeviceCreateInfo extends Structure {
	public static class ByValue extends VkDeviceGroupDeviceCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkDeviceGroupDeviceCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_GROUP_DEVICE_CREATE_INFO.value();
	public Pointer pNext;
	public int physicalDeviceCount;
	public Pointer pPhysicalDevices;
}
