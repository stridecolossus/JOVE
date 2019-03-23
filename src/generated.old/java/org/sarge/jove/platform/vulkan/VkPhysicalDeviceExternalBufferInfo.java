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
	"usage",
	"handleType"
})
public class VkPhysicalDeviceExternalBufferInfo extends Structure {
	public static class ByValue extends VkPhysicalDeviceExternalBufferInfo implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceExternalBufferInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_EXTERNAL_BUFFER_INFO.value();
	public Pointer pNext;
	public int flags;
	public int usage;
	public int handleType;
}
