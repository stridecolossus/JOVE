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
public class VkCommandBufferInheritanceInfo implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public Handle renderPass;
	public int subpass;
	public Handle framebuffer;
	public boolean occlusionQueryEnable;
	public EnumMask<VkQueryControlFlags> queryFlags;
	public EnumMask<VkQueryPipelineStatisticFlags> pipelineStatistics;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			POINTER.withName("renderPass"),
			JAVA_INT.withName("subpass"),
			PADDING,
			POINTER.withName("framebuffer"),
			JAVA_INT.withName("occlusionQueryEnable"),
			JAVA_INT.withName("queryFlags"),
			JAVA_INT.withName("pipelineStatistics"),
			PADDING
		);
	}
}
