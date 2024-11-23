package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.BitMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkMemoryHeap extends NativeStructure {
	public long size;
	public BitMask<VkMemoryHeapFlag> flags;

	@Override
	protected StructLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_LONG.withName("propertyFlags"),
				JAVA_INT.withName("heapIndex"),
                PADDING
		);
	}
}
