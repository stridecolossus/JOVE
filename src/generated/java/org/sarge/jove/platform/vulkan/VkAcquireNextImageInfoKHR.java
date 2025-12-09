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
public class VkAcquireNextImageInfoKHR implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public Handle swapchain;
	public long timeout;
	public Handle semaphore;
	public Handle fence;
	public int deviceMask;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			POINTER.withName("swapchain"),
			JAVA_LONG.withName("timeout"),
			POINTER.withName("semaphore"),
			POINTER.withName("fence"),
			JAVA_INT.withName("deviceMask"),
			PADDING
		);
	}
}
