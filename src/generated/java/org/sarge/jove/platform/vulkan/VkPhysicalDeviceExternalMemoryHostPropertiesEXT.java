package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
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
	"minImportedHostPointerAlignment"
})
public class VkPhysicalDeviceExternalMemoryHostPropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceExternalMemoryHostPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceExternalMemoryHostPropertiesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_EXTERNAL_MEMORY_HOST_PROPERTIES_EXT;
	public Pointer pNext;
	public long minImportedHostPointerAlignment;
}
