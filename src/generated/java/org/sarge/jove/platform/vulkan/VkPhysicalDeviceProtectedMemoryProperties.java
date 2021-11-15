package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

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
	"protectedNoFault"
})
public class VkPhysicalDeviceProtectedMemoryProperties extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceProtectedMemoryProperties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceProtectedMemoryProperties implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_PROTECTED_MEMORY_PROPERTIES;
	public Pointer pNext;
	public VulkanBoolean protectedNoFault;
}
