package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkMemoryAllocateInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.MEMORY_ALLOCATE_INFO;
	public Handle pNext;
	public long allocationSize;
	public int memoryTypeIndex;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				JAVA_LONG.withName("allocationSize"),
				JAVA_INT.withName("memoryTypeIndex"),
				PADDING
		);
	}
}
