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
	"deviceEvent"
})
public class VkDeviceEventInfoEXT extends Structure {
	public static class ByValue extends VkDeviceEventInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDeviceEventInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_EVENT_INFO_EXT.value();
	public Pointer pNext;
	public int deviceEvent;
}
