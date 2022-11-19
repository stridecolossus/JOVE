package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkShaderStage implements IntEnum {
 	VERTEX(1),
 	TESSELLATION_CONTROL(2),
 	TESSELLATION_EVALUATION(4),
 	GEOMETRY(8),
 	FRAGMENT(16),
 	COMPUTE(32),
 	ALL_GRAPHICS(31),
 	ALL(2147483647),
 	RAYGEN_NV(256),
 	ANY_HIT_NV(512),
 	CLOSEST_HIT_NV(1024),
 	MISS_NV(2048),
 	INTERSECTION_NV(4096),
 	CALLABLE_NV(8192),
 	TASK_NV(64),
 	MESH_NV(128);

	private final int value;

	private VkShaderStage(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
