package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

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
	"vulkanMemoryModel",
	"vulkanMemoryModelDeviceScope",
	"vulkanMemoryModelAvailabilityVisibilityChains"
})
public class VkPhysicalDeviceVulkanMemoryModelFeaturesKHR extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceVulkanMemoryModelFeaturesKHR implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceVulkanMemoryModelFeaturesKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_MEMORY_MODEL_FEATURES_KHR;
	public Pointer pNext;
	public VulkanBoolean vulkanMemoryModel;
	public VulkanBoolean vulkanMemoryModelDeviceScope;
	public VulkanBoolean vulkanMemoryModelAvailabilityVisibilityChains;
}
