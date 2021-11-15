package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
	public int aspectMask;
	public int mipLevel;
	public int baseArrayLayer;
	public int layerCount;
}
