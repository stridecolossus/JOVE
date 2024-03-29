package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"type",
	"generalShader",
	"closestHitShader",
	"anyHitShader",
	"intersectionShader"
})
public class VkRayTracingShaderGroupCreateInfoNV extends VulkanStructure {
	public static class ByValue extends VkRayTracingShaderGroupCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkRayTracingShaderGroupCreateInfoNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.RAY_TRACING_SHADER_GROUP_CREATE_INFO_NV;
	public Pointer pNext;
	public VkRayTracingShaderGroupTypeNV type;
	public int generalShader;
	public int closestHitShader;
	public int anyHitShader;
	public int intersectionShader;
}
