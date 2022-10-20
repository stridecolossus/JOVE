package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCoverageModulationModeNV implements IntegerEnumeration {
 	VK_COVERAGE_MODULATION_MODE_NONE_NV(0), 	
 	VK_COVERAGE_MODULATION_MODE_RGB_NV(1), 	
 	VK_COVERAGE_MODULATION_MODE_ALPHA_NV(2), 	
 	VK_COVERAGE_MODULATION_MODE_RGBA_NV(3), 	
 	VK_COVERAGE_MODULATION_MODE_BEGIN_RANGE_NV(0), 	
 	VK_COVERAGE_MODULATION_MODE_END_RANGE_NV(3), 	
 	VK_COVERAGE_MODULATION_MODE_RANGE_SIZE_NV(4), 	
 	VK_COVERAGE_MODULATION_MODE_MAX_ENUM_NV(2147483647); 	

	private final int value;
	
	private VkCoverageModulationModeNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
