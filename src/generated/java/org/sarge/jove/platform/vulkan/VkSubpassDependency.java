package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkSubpassDependency extends VulkanStructure {
	public int srcSubpass;
	public int dstSubpass;
	public BitMask<VkPipelineStage> srcStageMask;
	public BitMask<VkPipelineStage> dstStageMask;
	public BitMask<VkAccess> srcAccessMask;
	public BitMask<VkAccess> dstAccessMask;
	public BitMask<VkDependencyFlag> dependencyFlags;
}
