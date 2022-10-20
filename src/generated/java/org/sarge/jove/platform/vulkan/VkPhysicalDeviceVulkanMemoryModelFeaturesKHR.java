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
	"vulkanMemoryModel",
	"vulkanMemoryModelDeviceScope",
	"vulkanMemoryModelAvailabilityVisibilityChains"
})
public class VkPhysicalDeviceVulkanMemoryModelFeaturesKHR extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceVulkanMemoryModelFeaturesKHR implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceVulkanMemoryModelFeaturesKHR implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_VULKAN_MEMORY_MODEL_FEATURES_KHR;
	public Pointer pNext;
	public boolean vulkanMemoryModel;
	public boolean vulkanMemoryModelDeviceScope;
	public boolean vulkanMemoryModelAvailabilityVisibilityChains;
}
