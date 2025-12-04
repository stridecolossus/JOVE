package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.common.Handle;
import org.sarge.jove.util.EnumMask;
import org.sarge.jove.platform.vulkan.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkRayTracingPipelineCreateInfoNV implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public EnumMask<VkPipelineCreateFlags> flags;
	public int stageCount;
	public VkPipelineShaderStageCreateInfo[] pStages;
	public int groupCount;
	public VkRayTracingShaderGroupCreateInfoNV[] pGroups;
	public int maxRecursionDepth;
	public Handle layout;
	public Handle basePipelineHandle;
	public int basePipelineIndex;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("flags"),
			JAVA_INT.withName("stageCount"),
			POINTER.withName("pStages"),
			JAVA_INT.withName("groupCount"),
			PADDING,
			POINTER.withName("pGroups"),
			JAVA_INT.withName("maxRecursionDepth"),
			PADDING,
			POINTER.withName("layout"),
			POINTER.withName("basePipelineHandle"),
			JAVA_INT.withName("basePipelineIndex")
		);
	}
}
