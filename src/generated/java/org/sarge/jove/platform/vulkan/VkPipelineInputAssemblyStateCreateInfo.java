package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineInputAssemblyStateCreateInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public VkPrimitiveTopology topology;
	public boolean primitiveRestartEnable;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				JAVA_INT.withName("flags"),
				JAVA_INT.withName("topology"),
				JAVA_INT.withName("primitiveRestartEnable"),
				PADDING
		);
	}
}
