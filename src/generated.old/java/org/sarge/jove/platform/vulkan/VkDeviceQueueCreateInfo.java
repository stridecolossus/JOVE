package org.sarge.jove.platform.vulkan;

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
	"flags",
	"queueFamilyIndex",
	"queueCount",
	"pQueuePriorities"
})
public class VkDeviceQueueCreateInfo extends Structure {
	public static class ByValue extends VkDeviceQueueCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkDeviceQueueCreateInfo implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public int queueFamilyIndex;
	public int queueCount;
	public Pointer pQueuePriorities;
}
