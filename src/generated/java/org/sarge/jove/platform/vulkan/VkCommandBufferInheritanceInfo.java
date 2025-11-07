package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkCommandBufferInheritanceInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.COMMAND_BUFFER_INHERITANCE_INFO;
	public Pointer pNext;
	public Handle renderPass;
	public int subpass;
	public Handle framebuffer;
	public boolean occlusionQueryEnable;
	public VkQueryControlFlag queryFlags;
	public VkQueryPipelineStatisticFlag pipelineStatistics;

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
