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
	"subgroupSize",
	"supportedStages",
	"supportedOperations",
	"quadOperationsInAllStages"
})
public class VkPhysicalDeviceSubgroupProperties extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceSubgroupProperties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceSubgroupProperties implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SUBGROUP_PROPERTIES;
	public Pointer pNext;
	public int subgroupSize;
	public VkShaderStageFlags supportedStages;
	public VkSubgroupFeatureFlags supportedOperations;
	public VulkanBoolean quadOperationsInAllStages;
}
