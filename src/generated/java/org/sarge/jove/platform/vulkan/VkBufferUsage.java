package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkBufferUsage implements IntegerEnumeration {
 	TRANSFER_SRC(1),
 	TRANSFER_DST(2),
 	UNIFORM_TEXEL_BUFFER(4),
 	STORAGE_TEXEL_BUFFER(8),
 	UNIFORM_BUFFER(16),
 	STORAGE_BUFFER(32),
 	INDEX_BUFFER(64),
 	VERTEX_BUFFER(128),
 	INDIRECT_BUFFER(256),
 	TRANSFORM_FEEDBACK_BUFFER_EXT(2048),
 	TRANSFORM_FEEDBACK_COUNTER_BUFFER_EXT(4096),
 	CONDITIONAL_RENDERING_EXT(512),
 	RAY_TRACING_NV(1024),
 	SHADER_DEVICE_ADDRESS_EXT(131072);

	private final int value;

	private VkBufferUsage(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
