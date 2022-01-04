package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"drmFormatModifier",
	"drmFormatModifierPlaneCount",
	"drmFormatModifierTilingFeatures"
})
public class VkDrmFormatModifierPropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkDrmFormatModifierPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkDrmFormatModifierPropertiesEXT implements Structure.ByReference { }

	public long drmFormatModifier;
	public int drmFormatModifierPlaneCount;
	public VkFormatFeature drmFormatModifierTilingFeatures;
}
