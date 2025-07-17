package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkImageSubresourceRange extends VulkanStructure {
	public EnumMask<VkImageAspect> aspectMask;
	public int baseMipLevel;
	public int levelCount;
	public int baseArrayLayer;
	public int layerCount;
}
