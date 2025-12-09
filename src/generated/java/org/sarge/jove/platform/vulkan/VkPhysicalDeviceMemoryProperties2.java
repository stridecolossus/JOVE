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
public class VkPhysicalDeviceMemoryProperties2 implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public VkPhysicalDeviceMemoryProperties memoryProperties;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			MemoryLayout.structLayout(
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
			).withName("memoryProperties")
		);
	}
}
