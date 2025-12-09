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
public class VkDebugUtilsLabelEXT implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public String pLabelName;
	public float[] color;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			POINTER.withName("pLabelName"),
			MemoryLayout.sequenceLayout(4, JAVA_FLOAT).withName("color")
		);
	}
}
