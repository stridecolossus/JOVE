package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSamplerYcbcrModelConversion implements IntegerEnumeration {
 	VK_SAMPLER_YCBCR_MODEL_CONVERSION_RGB_IDENTITY(0), 	
 	VK_SAMPLER_YCBCR_MODEL_CONVERSION_YCBCR_IDENTITY(1), 	
 	VK_SAMPLER_YCBCR_MODEL_CONVERSION_YCBCR_709(2), 	
 	VK_SAMPLER_YCBCR_MODEL_CONVERSION_YCBCR_601(3), 	
 	VK_SAMPLER_YCBCR_MODEL_CONVERSION_YCBCR_2020(4), 	
 	VK_SAMPLER_YCBCR_MODEL_CONVERSION_RGB_IDENTITY_KHR(0), 	
 	VK_SAMPLER_YCBCR_MODEL_CONVERSION_YCBCR_IDENTITY_KHR(1), 	
 	VK_SAMPLER_YCBCR_MODEL_CONVERSION_YCBCR_709_KHR(2), 	
 	VK_SAMPLER_YCBCR_MODEL_CONVERSION_YCBCR_601_KHR(3), 	
 	VK_SAMPLER_YCBCR_MODEL_CONVERSION_YCBCR_2020_KHR(4), 	
 	VK_SAMPLER_YCBCR_MODEL_CONVERSION_BEGIN_RANGE(0), 	
 	VK_SAMPLER_YCBCR_MODEL_CONVERSION_END_RANGE(4), 	
 	VK_SAMPLER_YCBCR_MODEL_CONVERSION_RANGE_SIZE(5), 	
 	VK_SAMPLER_YCBCR_MODEL_CONVERSION_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkSamplerYcbcrModelConversion(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
