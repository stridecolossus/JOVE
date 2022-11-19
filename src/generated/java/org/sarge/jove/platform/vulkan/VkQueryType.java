package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkQueryType implements IntEnum {
 	OCCLUSION(0),
 	PIPELINE_STATISTICS(1),
 	TIMESTAMP(2),
 	TRANSFORM_FEEDBACK_STREAM_EXT(1000028004),
 	ACCELERATION_STRUCTURE_COMPACTED_SIZE_NV(1000165000);

	private final int value;

	private VkQueryType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
