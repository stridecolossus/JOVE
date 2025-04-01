package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkDeviceQueueCreateInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.DEVICE_QUEUE_CREATE_INFO;
	public Handle pNext;
	public EnumMask<VkDeviceQueueCreateFlag> flags;
	public int queueFamilyIndex;
	public int queueCount;
	public float[] pQueuePriorities;

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				JAVA_INT.withName("flags"),
				JAVA_INT.withName("queueFamilyIndex"),
				JAVA_INT.withName("queueCount"),
				PADDING,
				POINTER.withName("pQueuePriorities")
//			    ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(Integer.MAX_VALUE, ValueLayout.JAVA_FLOAT)).withName("pQueuePriorities")
		);
	}
}
