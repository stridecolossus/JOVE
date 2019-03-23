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
	"enabledValidationFeatureCount",
	"pEnabledValidationFeatures",
	"disabledValidationFeatureCount",
	"pDisabledValidationFeatures"
})
public class VkValidationFeaturesEXT extends Structure {
	public static class ByValue extends VkValidationFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkValidationFeaturesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_VALIDATION_FEATURES_EXT.value();
	public Pointer pNext;
	public int enabledValidationFeatureCount;
	public int pEnabledValidationFeatures;
	public int disabledValidationFeatureCount;
	public int pDisabledValidationFeatures;
}
