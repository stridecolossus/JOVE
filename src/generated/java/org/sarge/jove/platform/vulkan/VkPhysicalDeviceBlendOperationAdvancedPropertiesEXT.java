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

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_BLEND_OPERATION_ADVANCED_PROPERTIES_EXT;
	public Pointer pNext;
	public int advancedBlendMaxColorAttachments;
	public boolean advancedBlendIndependentBlend;
	public boolean advancedBlendNonPremultipliedSrcColor;
	public boolean advancedBlendNonPremultipliedDstColor;
	public boolean advancedBlendCorrelatedOverlap;
	public boolean advancedBlendAllOperations;
}
