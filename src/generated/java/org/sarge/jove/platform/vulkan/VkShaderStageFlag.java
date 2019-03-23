package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkShaderStageFlag implements IntegerEnumeration {
 	VK_SHADER_STAGE_VERTEX_BIT(1), 	
 	VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT(2), 	
 	VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT(4), 	
 	VK_SHADER_STAGE_GEOMETRY_BIT(8), 	
 	VK_SHADER_STAGE_FRAGMENT_BIT(16), 	
 	VK_SHADER_STAGE_COMPUTE_BIT(32), 	
 	VK_SHADER_STAGE_ALL_GRAPHICS(31), 	
 	VK_SHADER_STAGE_ALL(2147483647), 	
 	VK_SHADER_STAGE_RAYGEN_BIT_NV(256), 	
 	VK_SHADER_STAGE_ANY_HIT_BIT_NV(512), 	
 	VK_SHADER_STAGE_CLOSEST_HIT_BIT_NV(1024), 	
 	VK_SHADER_STAGE_MISS_BIT_NV(2048), 	
 	VK_SHADER_STAGE_INTERSECTION_BIT_NV(4096), 	
 	VK_SHADER_STAGE_CALLABLE_BIT_NV(8192), 	
 	VK_SHADER_STAGE_TASK_BIT_NV(64), 	
 	VK_SHADER_STAGE_MESH_BIT_NV(128), 	
 	VK_SHADER_STAGE_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkShaderStageFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
