package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkClearAttachment extends VulkanStructure {
	public EnumMask<VkImageAspect> aspectMask;
	public int colorAttachment;
	public VkClearValue clearValue;
}
