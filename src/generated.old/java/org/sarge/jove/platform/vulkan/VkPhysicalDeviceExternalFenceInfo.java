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
	"handleType"
})
public class VkPhysicalDeviceExternalFenceInfo extends Structure {
	public static class ByValue extends VkPhysicalDeviceExternalFenceInfo implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceExternalFenceInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_EXTERNAL_FENCE_INFO.value();
	public Pointer pNext;
	public int handleType;
}
