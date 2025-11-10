package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkRenderPassBeginInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.RENDER_PASS_BEGIN_INFO;
	public Handle pNext;
	public Handle renderPass;
	public Handle framebuffer;
	public VkRect2D renderArea;
	public int clearValueCount;
	public VkClearValue[] pClearValues;

	@Override
	public GroupLayout layout() {
	    return MemoryLayout.structLayout(
	            JAVA_INT.withName("sType"),
	            PADDING,
	            POINTER.withName("pNext"),
	            POINTER.withName("renderPass"),
	            POINTER.withName("framebuffer"),
	            MemoryLayout.structLayout(
	                MemoryLayout.structLayout(
	                    JAVA_INT.withName("x"),
	                    JAVA_INT.withName("y")
	                ).withName("offset"),
	                MemoryLayout.structLayout(
	                    JAVA_INT.withName("width"),
	                    JAVA_INT.withName("height")
	                ).withName("extent")
	            ).withName("renderArea"),
	            JAVA_INT.withName("clearValueCount"),
	            PADDING,
	            POINTER.withName("pClearValues")
	    );
	}
}
