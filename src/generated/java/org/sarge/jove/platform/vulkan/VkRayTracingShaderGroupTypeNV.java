package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkRayTracingShaderGroupTypeNV implements IntegerEnumeration {
 	VK_RAY_TRACING_SHADER_GROUP_TYPE_GENERAL_NV(0), 	
 	VK_RAY_TRACING_SHADER_GROUP_TYPE_TRIANGLES_HIT_GROUP_NV(1), 	
 	VK_RAY_TRACING_SHADER_GROUP_TYPE_PROCEDURAL_HIT_GROUP_NV(2), 	
 	VK_RAY_TRACING_SHADER_GROUP_TYPE_BEGIN_RANGE_NV(0), 	
 	VK_RAY_TRACING_SHADER_GROUP_TYPE_END_RANGE_NV(2), 	
 	VK_RAY_TRACING_SHADER_GROUP_TYPE_RANGE_SIZE_NV(3), 	
 	VK_RAY_TRACING_SHADER_GROUP_TYPE_MAX_ENUM_NV(2147483647); 	

	private final int value;
	
	private VkRayTracingShaderGroupTypeNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
