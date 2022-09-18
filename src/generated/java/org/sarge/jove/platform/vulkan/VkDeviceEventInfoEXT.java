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
	"deviceEvent"
})
public class VkDeviceEventInfoEXT extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DEVICE_EVENT_INFO_EXT;
	public Pointer pNext;
	public VkDeviceEventTypeEXT deviceEvent;
}
