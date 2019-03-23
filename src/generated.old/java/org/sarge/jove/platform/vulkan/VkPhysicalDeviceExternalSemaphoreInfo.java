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
public class VkPhysicalDeviceExternalSemaphoreInfo extends Structure {
	public static class ByValue extends VkPhysicalDeviceExternalSemaphoreInfo implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceExternalSemaphoreInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_EXTERNAL_SEMAPHORE_INFO.value();
	public Pointer pNext;
	public int handleType;
}
