package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkImageSubresourceLayers extends VulkanStructure {
	public EnumMask<VkImageAspect> aspectMask;
	public int mipLevel;
	public int baseArrayLayer;
	public int layerCount;
}
