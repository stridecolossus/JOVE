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
public class VkPhysicalDeviceMemoryProperties implements NativeStructure {
	public int memoryTypeCount;
	public VkMemoryType[] memoryTypes;
	public int memoryHeapCount;
	public VkMemoryHeap[] memoryHeaps;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("memoryTypeCount"),
			PADDING,
			MemoryLayout.sequenceLayout(32, MemoryLayout.structLayout(
				JAVA_INT.withName("propertyFlags"),
				JAVA_INT.withName("heapIndex")
			)).withName("memoryTypes"),
			JAVA_INT.withName("memoryHeapCount"),
			PADDING,
			MemoryLayout.sequenceLayout(16, MemoryLayout.structLayout(
				JAVA_LONG.withName("size"),
				JAVA_INT.withName("flags"),
				PADDING
			)).withName("memoryHeaps")
		);
	}
}
