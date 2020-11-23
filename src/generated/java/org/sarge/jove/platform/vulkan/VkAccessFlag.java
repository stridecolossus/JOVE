package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkAccessFlag implements IntegerEnumeration {
 	VK_ACCESS_INDIRECT_COMMAND_READ_BIT(1),
 	VK_ACCESS_INDEX_READ_BIT(2),
 	VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT(4),
 	VK_ACCESS_UNIFORM_READ_BIT(8),
 	VK_ACCESS_INPUT_ATTACHMENT_READ_BIT(16),
 	VK_ACCESS_SHADER_READ_BIT(32),
 	VK_ACCESS_SHADER_WRITE_BIT(64),
 	VK_ACCESS_COLOR_ATTACHMENT_READ_BIT(128),
 	VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT(256),
 	VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT(512),
 	VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT(1024),
 	VK_ACCESS_TRANSFER_READ_BIT(2048),
 	VK_ACCESS_TRANSFER_WRITE_BIT(4096),
 	VK_ACCESS_HOST_READ_BIT(8192),
 	VK_ACCESS_HOST_WRITE_BIT(16384),
 	VK_ACCESS_MEMORY_READ_BIT(32768),
 	VK_ACCESS_MEMORY_WRITE_BIT(65536),
 	VK_ACCESS_TRANSFORM_FEEDBACK_WRITE_BIT_EXT(33554432),
 	VK_ACCESS_TRANSFORM_FEEDBACK_COUNTER_READ_BIT_EXT(67108864),
 	VK_ACCESS_TRANSFORM_FEEDBACK_COUNTER_WRITE_BIT_EXT(134217728),
 	VK_ACCESS_CONDITIONAL_RENDERING_READ_BIT_EXT(1048576),
 	VK_ACCESS_COMMAND_PROCESS_READ_BIT_NVX(131072),
 	VK_ACCESS_COMMAND_PROCESS_WRITE_BIT_NVX(262144),
 	VK_ACCESS_COLOR_ATTACHMENT_READ_NONCOHERENT_BIT_EXT(524288),
 	VK_ACCESS_SHADING_RATE_IMAGE_READ_BIT_NV(8388608),
 	VK_ACCESS_ACCELERATION_STRUCTURE_READ_BIT_NV(2097152),
 	VK_ACCESS_ACCELERATION_STRUCTURE_WRITE_BIT_NV(4194304),
 	VK_ACCESS_FRAGMENT_DENSITY_MAP_READ_BIT_EXT(16777216);

	private final int value;

	private VkAccessFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
