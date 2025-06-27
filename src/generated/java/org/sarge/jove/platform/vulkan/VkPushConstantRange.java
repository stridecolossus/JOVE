package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPushConstantRange extends VulkanStructure {
	public EnumMask<VkShaderStage> stageFlags;
	public int offset;
	public int size;
}
