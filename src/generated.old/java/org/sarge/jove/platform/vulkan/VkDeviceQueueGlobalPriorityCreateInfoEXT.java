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
	"globalPriority"
})
public class VkDeviceQueueGlobalPriorityCreateInfoEXT extends Structure {
	public static class ByValue extends VkDeviceQueueGlobalPriorityCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDeviceQueueGlobalPriorityCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_QUEUE_GLOBAL_PRIORITY_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public int globalPriority;
}
