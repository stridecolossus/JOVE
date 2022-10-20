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
	"subgroupSize",
	"supportedStages",
	"supportedOperations",
	"quadOperationsInAllStages"
})
public class VkPhysicalDeviceSubgroupProperties extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceSubgroupProperties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceSubgroupProperties implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_SUBGROUP_PROPERTIES;
	public Pointer pNext;
	public int subgroupSize;
	public VkShaderStage supportedStages;
	public VkSubgroupFeatureFlag supportedOperations;
	public boolean quadOperationsInAllStages;
}
