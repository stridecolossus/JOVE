package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkSubpassDependency extends VulkanStructure {
	public int srcSubpass;
	public int dstSubpass;
	public EnumMask<VkPipelineStage> srcStageMask;
	public EnumMask<VkPipelineStage> dstStageMask;
	public EnumMask<VkAccess> srcAccessMask;
	public EnumMask<VkAccess> dstAccessMask;
	public EnumMask<VkDependencyFlag> dependencyFlags;
}
