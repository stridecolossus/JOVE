package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"handleType"
})
public class VkPhysicalDeviceExternalSemaphoreInfo extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceExternalSemaphoreInfo implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceExternalSemaphoreInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_EXTERNAL_SEMAPHORE_INFO;
	public Pointer pNext;
	public VkExternalSemaphoreHandleTypeFlag handleType;
}
