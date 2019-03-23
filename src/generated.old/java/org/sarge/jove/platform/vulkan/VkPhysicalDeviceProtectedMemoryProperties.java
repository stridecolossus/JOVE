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
	"protectedNoFault"
})
public class VkPhysicalDeviceProtectedMemoryProperties extends Structure {
	public static class ByValue extends VkPhysicalDeviceProtectedMemoryProperties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceProtectedMemoryProperties implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_PROTECTED_MEMORY_PROPERTIES.value();
	public Pointer pNext;
	public boolean protectedNoFault;
}
