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
	"advancedBlendMaxColorAttachments",
	"advancedBlendIndependentBlend",
	"advancedBlendNonPremultipliedSrcColor",
	"advancedBlendNonPremultipliedDstColor",
	"advancedBlendCorrelatedOverlap",
	"advancedBlendAllOperations"
})
public class VkPhysicalDeviceBlendOperationAdvancedPropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceBlendOperationAdvancedPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceBlendOperationAdvancedPropertiesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_BLEND_OPERATION_ADVANCED_PROPERTIES_EXT;
	public Pointer pNext;
	public int advancedBlendMaxColorAttachments;
	public VulkanBoolean advancedBlendIndependentBlend;
	public VulkanBoolean advancedBlendNonPremultipliedSrcColor;
	public VulkanBoolean advancedBlendNonPremultipliedDstColor;
	public VulkanBoolean advancedBlendCorrelatedOverlap;
	public VulkanBoolean advancedBlendAllOperations;
}
