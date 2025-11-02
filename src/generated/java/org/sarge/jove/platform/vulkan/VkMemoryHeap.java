package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkMemoryHeap implements NativeStructure {
	public long size;
	public EnumMask<VkMemoryHeapFlag> flags;

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_LONG.withName("size"),
				JAVA_INT.withName("flags"),
				PADDING
		);
	}

	@Override
	public String toString() {
		return String.format("size=%d flags=%s", size, flags);
	}
}
