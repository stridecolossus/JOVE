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
public class VkPipelineColorBlendStateCreateInfo implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int flags;
	public boolean logicOpEnable;
	public VkLogicOp logicOp;
	public int attachmentCount;
	public VkPipelineColorBlendAttachmentState[] pAttachments;
	public float[] blendConstants;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("flags"),
			JAVA_INT.withName("logicOpEnable"),
			JAVA_INT.withName("logicOp"),
			JAVA_INT.withName("attachmentCount"),
			POINTER.withName("pAttachments"),
			MemoryLayout.sequenceLayout(4, JAVA_FLOAT).withName("blendConstants")
		);
	}
}
