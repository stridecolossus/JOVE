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
	"advancedBlendCoherentOperations"
})
public class VkPhysicalDeviceBlendOperationAdvancedFeaturesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceBlendOperationAdvancedFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceBlendOperationAdvancedFeaturesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_BLEND_OPERATION_ADVANCED_FEATURES_EXT;
	public Pointer pNext;
	public VulkanBoolean advancedBlendCoherentOperations;
}
