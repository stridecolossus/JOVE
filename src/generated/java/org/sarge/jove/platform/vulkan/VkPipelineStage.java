package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPipelineStage implements IntegerEnumeration {
 	TOP_OF_PIPE(1),
 	DRAW_INDIRECT(2),
 	VERTEX_INPUT(4),
 	VERTEX_SHADER(8),
 	TESSELLATION_CONTROL_SHADER(16),
 	TESSELLATION_EVALUATION_SHADER(32),
 	GEOMETRY_SHADER(64),
 	FRAGMENT_SHADER(128),
 	EARLY_FRAGMENT_TESTS(256),
 	LATE_FRAGMENT_TESTS(512),
 	COLOR_ATTACHMENT_OUTPUT(1024),
 	COMPUTE_SHADER(2048),
 	TRANSFER(4096),
 	BOTTOM_OF_PIPE(8192),
 	HOST(16384),
 	ALL_GRAPHICS(32768),
 	ALL_COMMANDS(65536),
 	TRANSFORM_FEEDBACK_EXT(16777216),
 	CONDITIONAL_RENDERING_EXT(262144),
 	COMMAND_PROCESS_NVX(131072),
 	SHADING_RATE_IMAGE_NV(4194304),
 	RAY_TRACING_SHADER_NV(2097152),
 	ACCELERATION_STRUCTURE_BUILD_NV(33554432),
 	TASK_SHADER_NV(524288),
 	MESH_SHADER_NV(1048576),
 	FRAGMENT_DENSITY_PROCESS_EXT(8388608);

	private final int value;

	private VkPipelineStage(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
