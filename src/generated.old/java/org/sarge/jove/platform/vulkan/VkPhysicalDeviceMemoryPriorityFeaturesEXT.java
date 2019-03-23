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
	"memoryPriority"
})
public class VkPhysicalDeviceMemoryPriorityFeaturesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceMemoryPriorityFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMemoryPriorityFeaturesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MEMORY_PRIORITY_FEATURES_EXT.value();
	public Pointer pNext;
	public boolean memoryPriority;
}
