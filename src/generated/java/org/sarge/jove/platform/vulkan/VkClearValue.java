package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkClearValue extends VulkanStructure {
	public VkClearColorValue color;
	public VkClearDepthStencilValue depthStencil;

	@Override
	public UnionLayout layout() {
		return MemoryLayout.unionLayout(
			MemoryLayout.unionLayout(
		        MemoryLayout.sequenceLayout(4, JAVA_FLOAT).withName("float32"),
		        MemoryLayout.sequenceLayout(4, JAVA_INT).withName("int32"),
		        MemoryLayout.sequenceLayout(4, JAVA_INT).withName("uint32")
		    ).withName("color"),
		    MemoryLayout.structLayout(
		        JAVA_FLOAT.withName("depth"),
		        JAVA_INT.withName("stencil")
		    ).withName("depthStencil")
		);
	}
}
