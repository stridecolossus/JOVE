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
	"protectedMemory"
})
public class VkPhysicalDeviceProtectedMemoryFeatures extends Structure {
	public static class ByValue extends VkPhysicalDeviceProtectedMemoryFeatures implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceProtectedMemoryFeatures implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_PROTECTED_MEMORY_FEATURES.value();
	public Pointer pNext;
	public boolean protectedMemory;
}
