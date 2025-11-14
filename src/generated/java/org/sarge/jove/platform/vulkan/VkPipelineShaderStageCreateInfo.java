package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineShaderStageCreateInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.PIPELINE_SHADER_STAGE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public VkShaderStage stage;
	public Handle module;
	public String pName;
	public VkSpecializationInfo pSpecializationInfo;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				JAVA_INT.withName("flags"),
				JAVA_INT.withName("stage"),
				POINTER.withName("module"),
				POINTER.withName("pName"),
				POINTER.withName("pSpecializationInfo")
		);
	}
}
