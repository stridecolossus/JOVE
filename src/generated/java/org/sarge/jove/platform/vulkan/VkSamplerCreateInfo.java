package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"magFilter",
	"minFilter",
	"mipmapMode",
	"addressModeU",
	"addressModeV",
	"addressModeW",
	"mipLodBias",
	"anisotropyEnable",
	"maxAnisotropy",
	"compareEnable",
	"compareOp",
	"minLod",
	"maxLod",
	"borderColor",
	"unnormalizedCoordinates"
})
public class VkSamplerCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.SAMPLER_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public VkFilter magFilter;
	public VkFilter minFilter;
	public VkSamplerMipmapMode mipmapMode;
	public VkSamplerAddressMode addressModeU;
	public VkSamplerAddressMode addressModeV;
	public VkSamplerAddressMode addressModeW;
	public float mipLodBias;
	public VulkanBoolean anisotropyEnable;
	public float maxAnisotropy;
	public VulkanBoolean compareEnable;
	public VkCompareOp compareOp;
	public float minLod;
	public float maxLod;
	public VkBorderColor borderColor;
	public VulkanBoolean unnormalizedCoordinates;
}
