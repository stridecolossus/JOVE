package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
public class VkPhysicalDeviceExternalFenceInfo extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceExternalFenceInfo implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceExternalFenceInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_EXTERNAL_FENCE_INFO;
	public Pointer pNext;
	public VkExternalFenceHandleTypeFlag handleType;
}
