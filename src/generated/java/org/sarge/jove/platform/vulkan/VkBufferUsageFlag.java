package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkBufferUsageFlag implements IntegerEnumeration {
 	VK_BUFFER_USAGE_TRANSFER_SRC_BIT(1),
 	VK_BUFFER_USAGE_TRANSFER_DST_BIT(2),
 	VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT(4),
 	VK_BUFFER_USAGE_STORAGE_TEXEL_BUFFER_BIT(8),
 	VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT(16),
 	VK_BUFFER_USAGE_STORAGE_BUFFER_BIT(32),
 	VK_BUFFER_USAGE_INDEX_BUFFER_BIT(64),
 	VK_BUFFER_USAGE_VERTEX_BUFFER_BIT(128),
 	VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT(256),
 	VK_BUFFER_USAGE_TRANSFORM_FEEDBACK_BUFFER_BIT_EXT(2048),
 	VK_BUFFER_USAGE_TRANSFORM_FEEDBACK_COUNTER_BUFFER_BIT_EXT(4096),
 	VK_BUFFER_USAGE_CONDITIONAL_RENDERING_BIT_EXT(512),
 	VK_BUFFER_USAGE_RAY_TRACING_BIT_NV(1024),
 	VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT_EXT(131072);

	private final int value;

	private VkBufferUsageFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
