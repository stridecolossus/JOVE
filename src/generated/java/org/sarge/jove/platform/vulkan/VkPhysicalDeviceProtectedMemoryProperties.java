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
	"protectedNoFault"
})
public class VkPhysicalDeviceProtectedMemoryProperties extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceProtectedMemoryProperties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceProtectedMemoryProperties implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_PROTECTED_MEMORY_PROPERTIES;
	public Pointer pNext;
	public boolean protectedNoFault;
}
