package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkClearColorValue extends VulkanStructure {
	public float[] float32 = new float[4];
	public int[] int32 = new int[4];
	public int[] uint32 = new int[4];

	@Override
	public UnionLayout layout() {
		return MemoryLayout.unionLayout(
		        MemoryLayout.sequenceLayout(4, JAVA_FLOAT).withName("float32"),
		        MemoryLayout.sequenceLayout(4, JAVA_INT).withName("int32"),
		        MemoryLayout.sequenceLayout(4, JAVA_INT).withName("uint32")
	    );
	}
}
