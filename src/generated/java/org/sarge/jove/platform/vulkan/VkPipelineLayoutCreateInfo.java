package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineLayoutCreateInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.PIPELINE_LAYOUT_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public int setLayoutCount;
	public Handle[] pSetLayouts;
	public int pushConstantRangeCount;
	public VkPushConstantRange[] pPushConstantRanges;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				JAVA_INT.withName("flags"),
				JAVA_INT.withName("setLayoutCount"),
				POINTER.withName("pSetLayouts"),
				JAVA_INT.withName("pushConstantRangeCount"),
				PADDING,
				POINTER.withName("pPushConstantRanges")
		);
	}
}
