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
	"vulkanMemoryModel",
	"vulkanMemoryModelDeviceScope",
	"vulkanMemoryModelAvailabilityVisibilityChains"
})
public class VkPhysicalDeviceVulkanMemoryModelFeaturesKHR extends Structure {
	public static class ByValue extends VkPhysicalDeviceVulkanMemoryModelFeaturesKHR implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceVulkanMemoryModelFeaturesKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_MEMORY_MODEL_FEATURES_KHR.value();
	public Pointer pNext;
	public boolean vulkanMemoryModel;
	public boolean vulkanMemoryModelDeviceScope;
	public boolean vulkanMemoryModelAvailabilityVisibilityChains;
}
