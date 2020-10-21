package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"physicalDeviceCount",
	"pPhysicalDevices"
})
public class VkDeviceGroupDeviceCreateInfo extends VulkanStructure {
	public static class ByValue extends VkDeviceGroupDeviceCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkDeviceGroupDeviceCreateInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_GROUP_DEVICE_CREATE_INFO;
	public Pointer pNext;
	public int physicalDeviceCount;
	public Pointer pPhysicalDevices;
}
