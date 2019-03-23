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
	"deviceMask"
})
public class VkDeviceGroupCommandBufferBeginInfo extends Structure {
	public static class ByValue extends VkDeviceGroupCommandBufferBeginInfo implements Structure.ByValue { }
	public static class ByReference extends VkDeviceGroupCommandBufferBeginInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_GROUP_COMMAND_BUFFER_BEGIN_INFO.value();
	public Pointer pNext;
	public int deviceMask;
}
