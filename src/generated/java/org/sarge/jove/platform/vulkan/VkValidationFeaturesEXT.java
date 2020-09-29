package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"enabledValidationFeatureCount",
	"pEnabledValidationFeatures",
	"disabledValidationFeatureCount",
	"pDisabledValidationFeatures"
})
public class VkValidationFeaturesEXT extends VulkanStructure {
	public static class ByValue extends VkValidationFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkValidationFeaturesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_VALIDATION_FEATURES_EXT;
	public Pointer pNext;
	public int enabledValidationFeatureCount;
	public Pointer pEnabledValidationFeatures;
	public int disabledValidationFeatureCount;
	public Pointer pDisabledValidationFeatures;
}
