package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

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
	public BitMask<VkFormatFeature> linearTilingFeatures;
	public BitMask<VkFormatFeature> optimalTilingFeatures;
	public BitMask<VkFormatFeature> bufferFeatures;
}
