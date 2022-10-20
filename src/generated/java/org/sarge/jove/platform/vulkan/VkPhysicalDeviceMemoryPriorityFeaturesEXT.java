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
	"memoryPriority"
})
public class VkPhysicalDeviceMemoryPriorityFeaturesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceMemoryPriorityFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMemoryPriorityFeaturesEXT implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_MEMORY_PRIORITY_FEATURES_EXT;
	public Pointer pNext;
	public boolean memoryPriority;
}
