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
public class VkPipelineDepthStencilStateCreateInfo implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int flags;
	public boolean depthTestEnable;
	public boolean depthWriteEnable;
	public VkCompareOp depthCompareOp;
	public boolean depthBoundsTestEnable;
	public boolean stencilTestEnable;
	public VkStencilOpState front;
	public VkStencilOpState back;
	public float minDepthBounds;
	public float maxDepthBounds;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("flags"),
			JAVA_INT.withName("depthTestEnable"),
			JAVA_INT.withName("depthWriteEnable"),
			JAVA_INT.withName("depthCompareOp"),
			JAVA_INT.withName("depthBoundsTestEnable"),
			JAVA_INT.withName("stencilTestEnable"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("failOp"),
				JAVA_INT.withName("passOp"),
				JAVA_INT.withName("depthFailOp"),
				JAVA_INT.withName("compareOp"),
				JAVA_INT.withName("compareMask"),
				JAVA_INT.withName("writeMask"),
				JAVA_INT.withName("reference")
			).withName("front"),
			PADDING,
			MemoryLayout.structLayout(
				JAVA_INT.withName("failOp"),
				JAVA_INT.withName("passOp"),
				JAVA_INT.withName("depthFailOp"),
				JAVA_INT.withName("compareOp"),
				JAVA_INT.withName("compareMask"),
				JAVA_INT.withName("writeMask"),
				JAVA_INT.withName("reference")
			).withName("back"),
			JAVA_FLOAT.withName("minDepthBounds"),
			JAVA_FLOAT.withName("maxDepthBounds")
		);
	}
}
