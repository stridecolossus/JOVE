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
	"globalPriority"
})
public class VkDeviceQueueGlobalPriorityCreateInfoEXT extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DEVICE_QUEUE_GLOBAL_PRIORITY_CREATE_INFO_EXT;
	public Pointer pNext;
	public VkQueueGlobalPriorityEXT globalPriority;
}
