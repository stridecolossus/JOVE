package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"aspectMask",
	"baseMipLevel",
	"levelCount",
	"baseArrayLayer",
	"layerCount"
})
public class VkImageSubresourceRange extends VulkanStructure {
	public BitMask<VkImageAspect> aspectMask;
	public int baseMipLevel;
	public int levelCount;
	public int baseArrayLayer;
	public int layerCount;
}
