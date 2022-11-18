package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitField;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"aspectMask",
	"mipLevel",
	"baseArrayLayer",
	"layerCount"
})
public class VkImageSubresourceLayers extends VulkanStructure {
	public BitField<VkImageAspect> aspectMask;
	public int mipLevel;
	public int baseArrayLayer;
	public int layerCount;
}
