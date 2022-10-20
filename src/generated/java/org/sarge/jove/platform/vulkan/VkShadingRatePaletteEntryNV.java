package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkShadingRatePaletteEntryNV implements IntegerEnumeration {
 	VK_SHADING_RATE_PALETTE_ENTRY_NO_INVOCATIONS_NV(0), 	
 	VK_SHADING_RATE_PALETTE_ENTRY_16_INVOCATIONS_PER_PIXEL_NV(1), 	
 	VK_SHADING_RATE_PALETTE_ENTRY_8_INVOCATIONS_PER_PIXEL_NV(2), 	
 	VK_SHADING_RATE_PALETTE_ENTRY_4_INVOCATIONS_PER_PIXEL_NV(3), 	
 	VK_SHADING_RATE_PALETTE_ENTRY_2_INVOCATIONS_PER_PIXEL_NV(4), 	
 	VK_SHADING_RATE_PALETTE_ENTRY_1_INVOCATION_PER_PIXEL_NV(5), 	
 	VK_SHADING_RATE_PALETTE_ENTRY_1_INVOCATION_PER_2X1_PIXELS_NV(6), 	
 	VK_SHADING_RATE_PALETTE_ENTRY_1_INVOCATION_PER_1X2_PIXELS_NV(7), 	
 	VK_SHADING_RATE_PALETTE_ENTRY_1_INVOCATION_PER_2X2_PIXELS_NV(8), 	
 	VK_SHADING_RATE_PALETTE_ENTRY_1_INVOCATION_PER_4X2_PIXELS_NV(9), 	
 	VK_SHADING_RATE_PALETTE_ENTRY_1_INVOCATION_PER_2X4_PIXELS_NV(10), 	
 	VK_SHADING_RATE_PALETTE_ENTRY_1_INVOCATION_PER_4X4_PIXELS_NV(11), 	
 	VK_SHADING_RATE_PALETTE_ENTRY_BEGIN_RANGE_NV(0), 	
 	VK_SHADING_RATE_PALETTE_ENTRY_END_RANGE_NV(11), 	
 	VK_SHADING_RATE_PALETTE_ENTRY_RANGE_SIZE_NV(12), 	
 	VK_SHADING_RATE_PALETTE_ENTRY_MAX_ENUM_NV(2147483647); 	

	private final int value;
	
	private VkShadingRatePaletteEntryNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
