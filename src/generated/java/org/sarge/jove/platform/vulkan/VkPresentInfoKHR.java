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
public class VkPresentInfoKHR implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int waitSemaphoreCount;
	public Handle[] pWaitSemaphores;
	public int swapchainCount;
	public Handle[] pSwapchains;
	public Handle pImageIndices;
	public Handle pResults;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("waitSemaphoreCount"),
			PADDING,
			POINTER.withName("pWaitSemaphores"),
			JAVA_INT.withName("swapchainCount"),
			PADDING,
			POINTER.withName("pSwapchains"),
			POINTER.withName("pImageIndices"),
			POINTER.withName("pResults")
		);
	}
}
