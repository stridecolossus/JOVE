package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

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
public class VkRayTracingShaderGroupCreateInfoNV extends Structure {
	public static class ByValue extends VkRayTracingShaderGroupCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkRayTracingShaderGroupCreateInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_RAY_TRACING_SHADER_GROUP_CREATE_INFO_NV.value();
	public Pointer pNext;
	public int type;
	public int generalShader;
	public int closestHitShader;
	public int anyHitShader;
	public int intersectionShader;
}
