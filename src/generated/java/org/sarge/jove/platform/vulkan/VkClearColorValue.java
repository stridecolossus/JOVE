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
public class VkClearColorValue implements NativeStructure {
	public float[] float32;
	public int[] int32;
	public int[] uint32;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.unionLayout(
			MemoryLayout.sequenceLayout(4, JAVA_FLOAT).withName("float32"),
			MemoryLayout.sequenceLayout(4, JAVA_INT).withName("int32"),
			MemoryLayout.sequenceLayout(4, JAVA_INT).withName("uint32")
		);
	}
}
