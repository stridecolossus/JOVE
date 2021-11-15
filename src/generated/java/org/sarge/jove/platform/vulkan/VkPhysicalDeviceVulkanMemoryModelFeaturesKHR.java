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
	"vulkanMemoryModel",
	"vulkanMemoryModelDeviceScope",
	"vulkanMemoryModelAvailabilityVisibilityChains"
})
public class VkPhysicalDeviceVulkanMemoryModelFeaturesKHR extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceVulkanMemoryModelFeaturesKHR implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceVulkanMemoryModelFeaturesKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_VULKAN_MEMORY_MODEL_FEATURES_KHR;
	public Pointer pNext;
	public VulkanBoolean vulkanMemoryModel;
	public VulkanBoolean vulkanMemoryModelDeviceScope;
	public VulkanBoolean vulkanMemoryModelAvailabilityVisibilityChains;
}
