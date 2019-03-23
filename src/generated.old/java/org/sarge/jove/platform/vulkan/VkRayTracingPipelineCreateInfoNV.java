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
	"flags",
	"stageCount",
	"pStages",
	"groupCount",
	"pGroups",
	"maxRecursionDepth",
	"layout",
	"basePipelineHandle",
	"basePipelineIndex"
})
public class VkRayTracingPipelineCreateInfoNV extends Structure {
	public static class ByValue extends VkRayTracingPipelineCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkRayTracingPipelineCreateInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_RAY_TRACING_PIPELINE_CREATE_INFO_NV.value();
	public Pointer pNext;
	public int flags;
	public int stageCount;
	public VkPipelineShaderStageCreateInfo.ByReference pStages;
	public int groupCount;
	public VkRayTracingShaderGroupCreateInfoNV.ByReference pGroups;
	public int maxRecursionDepth;
	public long layout;
	public long basePipelineHandle;
	public int basePipelineIndex;
}
