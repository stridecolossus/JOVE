package org.sarge.jove.platform.vulkan;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkFenceCreateInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.FENCE_CREATE_INFO;
	public Handle pNext;
	public EnumMask<VkFenceCreateFlag> flags;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				ValueLayout.JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				ValueLayout.JAVA_INT.withName("flags"),
				PADDING
		);
	}
}
