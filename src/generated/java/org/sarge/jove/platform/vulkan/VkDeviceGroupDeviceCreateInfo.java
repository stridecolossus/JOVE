package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
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
	public VkStructureType sType = VkStructureType.DEVICE_GROUP_DEVICE_CREATE_INFO;
	public Pointer pNext;
	public int physicalDeviceCount;
	public Pointer pPhysicalDevices;
}
