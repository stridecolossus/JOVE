package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.BitMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkMemoryHeap implements NativeStructure {
	public long size;
	public BitMask<VkMemoryHeapFlag> flags;

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_LONG.withName("size"),
				JAVA_INT.withName("flags")
		);
	}
}
