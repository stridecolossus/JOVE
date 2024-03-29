package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDynamicState implements IntEnum {
 	VIEWPORT(0),
 	SCISSOR(1),
 	LINE_WIDTH(2),
 	DEPTH_BIAS(3),
 	BLEND_CONSTANTS(4),
 	DEPTH_BOUNDS(5),
 	STENCIL_COMPARE_MASK(6),
 	STENCIL_WRITE_MASK(7),
 	STENCIL_REFERENCE(8),
 	VIEWPORT_W_SCALING_NV(1000087000),
 	DISCARD_RECTANGLE_EXT(1000099000),
 	SAMPLE_LOCATIONS_EXT(1000143000),
 	VIEWPORT_SHADING_RATE_PALETTE_NV(1000164004),
 	VIEWPORT_COARSE_SAMPLE_ORDER_NV(1000164006),
 	EXCLUSIVE_SCISSOR_NV(1000205001);

	private final int value;

	private VkDynamicState(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
