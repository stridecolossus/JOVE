package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.*;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"protectedMemory"
})
public class VkPhysicalDeviceProtectedMemoryFeatures extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceProtectedMemoryFeatures implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceProtectedMemoryFeatures implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_PROTECTED_MEMORY_FEATURES;
	public Pointer pNext;
	public boolean protectedMemory;
}
