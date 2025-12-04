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
public class VkSparseImageOpaqueMemoryBindInfo implements NativeStructure {
	public Handle image;
	public int bindCount;
	public VkSparseMemoryBind[] pBinds;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			POINTER.withName("image"),
			JAVA_INT.withName("bindCount"),
			PADDING,
			POINTER.withName("pBinds")
		);
	}
}
