package org.sarge.jove.platform.vulkan;

import java.lang.foreign.StructLayout;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.BitMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkDeviceQueueCreateInfo extends NativeStructure {
	public final VkStructureType sType = VkStructureType.DEVICE_QUEUE_CREATE_INFO;
	public Handle pNext;
	public BitMask<VkDeviceQueueCreateFlag> flags;
	public int queueFamilyIndex;
	public int queueCount;
	public float[] pQueuePriorities;

	@Override
	protected StructLayout layout() {
		// TODO
		return null;
	}
}
