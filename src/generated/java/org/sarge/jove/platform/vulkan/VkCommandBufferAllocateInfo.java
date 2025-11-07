package org.sarge.jove.platform.vulkan;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkCommandBufferAllocateInfo implements NativeStructure {
	public VkStructureType sType = VkStructureType.COMMAND_BUFFER_ALLOCATE_INFO;
	public Handle pNext;
	public Handle commandPool;
	public VkCommandBufferLevel level;
	public int commandBufferCount;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				ValueLayout.JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				POINTER.withName("commandPool"),
				ValueLayout.JAVA_INT.withName("level"),
				ValueLayout.JAVA_INT.withName("commandBufferCount")
		);
	}
}
