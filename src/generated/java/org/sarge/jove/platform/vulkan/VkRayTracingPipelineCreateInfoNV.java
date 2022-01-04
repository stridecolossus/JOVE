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
public class VkRayTracingPipelineCreateInfoNV extends VulkanStructure {
	public static class ByValue extends VkRayTracingPipelineCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkRayTracingPipelineCreateInfoNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.RAY_TRACING_PIPELINE_CREATE_INFO_NV;
	public Pointer pNext;
	public int flags;
	public int stageCount;
	public Pointer pStages;
	public int groupCount;
	public Pointer pGroups;
	public int maxRecursionDepth;
	public Pointer layout;
	public Pointer basePipelineHandle;
	public int basePipelineIndex;
}
