package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkClearDepthStencilValue extends VulkanStructure {
	public float depth;
	public int stencil;

	@Override
	public GroupLayout layout() {
	    return MemoryLayout.structLayout(
	        JAVA_FLOAT.withName("depth"),
	        JAVA_INT.withName("stencil")
	    );
	}
}
