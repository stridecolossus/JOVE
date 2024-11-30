package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPhysicalDeviceMemoryProperties implements NativeStructure {
	public int memoryTypeCount;
	public VkMemoryType[] memoryTypes = new VkMemoryType[32];
	public int memoryHeapCount;
	public VkMemoryHeap[] memoryHeaps = new VkMemoryHeap[16];

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
	            JAVA_INT.withName("memoryTypeCount"),
	            MemoryLayout.sequenceLayout(32, MemoryLayout.structLayout(
	                JAVA_INT.withName("propertyFlags"),
	                JAVA_INT.withName("heapIndex")
	            )).withName("memoryTypes"),
	            JAVA_INT.withName("memoryHeapCount"),
	            MemoryLayout.sequenceLayout(16, MemoryLayout.structLayout(
	                JAVA_LONG.withName("size"),
	                JAVA_INT.withName("flags"),
	                PADDING
	            )).withName("memoryHeaps")
        );
	}
}
