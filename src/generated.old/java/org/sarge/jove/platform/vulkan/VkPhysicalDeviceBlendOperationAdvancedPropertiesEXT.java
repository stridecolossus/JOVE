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
	"advancedBlendMaxColorAttachments",
	"advancedBlendIndependentBlend",
	"advancedBlendNonPremultipliedSrcColor",
	"advancedBlendNonPremultipliedDstColor",
	"advancedBlendCorrelatedOverlap",
	"advancedBlendAllOperations"
})
public class VkPhysicalDeviceBlendOperationAdvancedPropertiesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceBlendOperationAdvancedPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceBlendOperationAdvancedPropertiesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_BLEND_OPERATION_ADVANCED_PROPERTIES_EXT.value();
	public Pointer pNext;
	public int advancedBlendMaxColorAttachments;
	public boolean advancedBlendIndependentBlend;
	public boolean advancedBlendNonPremultipliedSrcColor;
	public boolean advancedBlendNonPremultipliedDstColor;
	public boolean advancedBlendCorrelatedOverlap;
	public boolean advancedBlendAllOperations;
}
