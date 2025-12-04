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
public class VkBindSparseInfo implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int waitSemaphoreCount;
	public Handle[] pWaitSemaphores;
	public int bufferBindCount;
	public VkSparseBufferMemoryBindInfo[] pBufferBinds;
	public int imageOpaqueBindCount;
	public VkSparseImageOpaqueMemoryBindInfo[] pImageOpaqueBinds;
	public int imageBindCount;
	public VkSparseImageMemoryBindInfo[] pImageBinds;
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
			JAVA_INT.withName("bufferBindCount"),
			PADDING,
			POINTER.withName("pBufferBinds"),
			JAVA_INT.withName("imageOpaqueBindCount"),
			PADDING,
			POINTER.withName("pImageOpaqueBinds"),
			JAVA_INT.withName("imageBindCount"),
			PADDING,
			POINTER.withName("pImageBinds"),
			JAVA_INT.withName("signalSemaphoreCount"),
			PADDING,
			POINTER.withName("pSignalSemaphores")
		);
	}
}
