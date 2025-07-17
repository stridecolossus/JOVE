package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkDescriptorSetLayoutBinding extends VulkanStructure {
	public int binding;
	public VkDescriptorType descriptorType;
	public int descriptorCount;
	public EnumMask<VkShaderStage> stageFlags;
	public Handle pImmutableSamplers;
}
