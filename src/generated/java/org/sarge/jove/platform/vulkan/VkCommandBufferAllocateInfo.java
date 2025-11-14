package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkCommandBufferAllocateInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.COMMAND_BUFFER_ALLOCATE_INFO;
	public Handle pNext;
	public Handle commandPool;
	public VkCommandBufferLevel level;
	public int commandBufferCount;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				POINTER.withName("commandPool"),
				JAVA_INT.withName("level"),
				JAVA_INT.withName("commandBufferCount")
		);
	}
}
