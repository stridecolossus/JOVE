package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkSamplerCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.SAMPLER_CREATE_INFO;
	public Handle pNext;
	public BitMask<VkSamplerCreateFlag> flags;
	public VkFilter magFilter;
	public VkFilter minFilter;
	public VkSamplerMipmapMode mipmapMode;
	public VkSamplerAddressMode addressModeU;
	public VkSamplerAddressMode addressModeV;
	public VkSamplerAddressMode addressModeW;
	public float mipLodBias;
	public boolean anisotropyEnable;
	public float maxAnisotropy;
	public boolean compareEnable;
	public VkCompareOp compareOp;
	public float minLod;
	public float maxLod;
	public VkBorderColor borderColor;
	public boolean unnormalizedCoordinates;
}
