package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitField;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"linearTilingFeatures",
	"optimalTilingFeatures",
	"bufferFeatures"
})
public class VkFormatProperties extends VulkanStructure {
	public BitField<VkFormatFeature> linearTilingFeatures;
	public BitField<VkFormatFeature> optimalTilingFeatures;
	public BitField<VkFormatFeature> bufferFeatures;
}
