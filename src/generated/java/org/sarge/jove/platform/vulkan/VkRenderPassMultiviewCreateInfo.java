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
public class VkRenderPassMultiviewCreateInfo implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int subpassCount;
	public Handle pViewMasks;
	public int dependencyCount;
	public Handle pViewOffsets;
	public int correlationMaskCount;
	public Handle pCorrelationMasks;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("subpassCount"),
			PADDING,
			POINTER.withName("pViewMasks"),
			JAVA_INT.withName("dependencyCount"),
			PADDING,
			POINTER.withName("pViewOffsets"),
			JAVA_INT.withName("correlationMaskCount"),
			PADDING,
			POINTER.withName("pCorrelationMasks")
		);
	}
}
