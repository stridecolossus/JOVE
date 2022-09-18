package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
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
	public VkStructureType sType = VkStructureType.VALIDATION_FEATURES_EXT;
	public Pointer pNext;
	public int enabledValidationFeatureCount;
	public Pointer pEnabledValidationFeatures;
	public int disabledValidationFeatureCount;
	public Pointer pDisabledValidationFeatures;
}
