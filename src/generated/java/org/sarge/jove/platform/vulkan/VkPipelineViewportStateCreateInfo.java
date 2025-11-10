package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineViewportStateCreateInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.PIPELINE_VIEWPORT_STATE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public int viewportCount;
	public VkViewport[] pViewports;
	public int scissorCount;
	public VkRect2D[] pScissors;

	@Override
	public GroupLayout layout() {
	    return MemoryLayout.structLayout(
	            JAVA_INT.withName("sType"),
	            PADDING,
	            POINTER.withName("pNext"),
	            JAVA_INT.withName("flags"),
	            JAVA_INT.withName("viewportCount"),
	            POINTER.withName("pViewports"),
	            JAVA_INT.withName("scissorCount"),
	            PADDING,
	            POINTER.withName("pScissors")
	    );
	}
}
