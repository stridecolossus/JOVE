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
	"flags",
	"queueFamilyIndex",
	"queueIndex"
})
public class VkDeviceQueueInfo2 extends Structure {
	public static class ByValue extends VkDeviceQueueInfo2 implements Structure.ByValue { }
	public static class ByReference extends VkDeviceQueueInfo2 implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_QUEUE_INFO_2.value();
	public Pointer pNext;
	public int flags;
	public int queueFamilyIndex;
	public int queueIndex;
}
