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
public class VkConditionalRenderingBeginInfoEXT implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public Handle buffer;
	public long offset;
	public EnumMask<VkConditionalRenderingFlagsEXT> flags;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			POINTER.withName("buffer"),
			JAVA_LONG.withName("offset"),
			JAVA_INT.withName("flags")
		);
	}
}
