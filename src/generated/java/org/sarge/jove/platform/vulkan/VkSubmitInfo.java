package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkSubmitInfo implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int waitSemaphoreCount;
	public Handle[] pWaitSemaphores;
	public int[] pWaitDstStageMask;
	public int commandBufferCount;
	public Handle[] pCommandBuffers;
	public int signalSemaphoreCount;
	public Handle[] pSignalSemaphores;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("waitSemaphoreCount"),
			PADDING,
			POINTER.withName("pWaitSemaphores"),
			POINTER.withName("pWaitDstStageMask"),
			JAVA_INT.withName("commandBufferCount"),
			PADDING,
			POINTER.withName("pCommandBuffers"),
			JAVA_INT.withName("signalSemaphoreCount"),
			PADDING,
			POINTER.withName("pSignalSemaphores")
		);
	}
}
