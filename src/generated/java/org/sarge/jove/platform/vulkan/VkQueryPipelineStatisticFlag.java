package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkQueryPipelineStatisticFlag implements IntegerEnumeration {
 	INPUT_ASSEMBLY_VERTICES(1),
 	INPUT_ASSEMBLY_PRIMITIVES(2),
 	VERTEX_SHADER_INVOCATIONS(4),
 	GEOMETRY_SHADER_INVOCATIONS(8),
 	GEOMETRY_SHADER_PRIMITIVES(16),
 	CLIPPING_INVOCATIONS(32),
 	CLIPPING_PRIMITIVES(64),
 	FRAGMENT_SHADER_INVOCATIONS(128),
 	TESSELLATION_CONTROL_SHADER_PATCHES(256),
 	TESSELLATION_EVALUATION_SHADER_INVOCATIONS(512),
 	COMPUTE_SHADER_INVOCATIONS(1024);

	private final int value;

	private VkQueryPipelineStatisticFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
