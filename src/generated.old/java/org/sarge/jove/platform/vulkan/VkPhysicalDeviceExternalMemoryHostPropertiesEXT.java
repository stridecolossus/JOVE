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
	"minImportedHostPointerAlignment"
})
public class VkPhysicalDeviceExternalMemoryHostPropertiesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceExternalMemoryHostPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceExternalMemoryHostPropertiesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_EXTERNAL_MEMORY_HOST_PROPERTIES_EXT.value();
	public Pointer pNext;
	public long minImportedHostPointerAlignment;
}
