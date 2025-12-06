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
public class VkDeviceGroupSubmitInfo implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int waitSemaphoreCount;
	public int[] pWaitSemaphoreDeviceIndices;
	public int commandBufferCount;
	public int[] pCommandBufferDeviceMasks;
	public int signalSemaphoreCount;
	public int[] pSignalSemaphoreDeviceIndices;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("waitSemaphoreCount"),
			PADDING,
			POINTER.withName("pWaitSemaphoreDeviceIndices"),
			JAVA_INT.withName("commandBufferCount"),
			PADDING,
			POINTER.withName("pCommandBufferDeviceMasks"),
			JAVA_INT.withName("signalSemaphoreCount"),
			PADDING,
			POINTER.withName("pSignalSemaphoreDeviceIndices")
		);
	}
}
