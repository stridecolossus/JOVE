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
public class VkDeviceGroupDeviceCreateInfo implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int physicalDeviceCount;
	public Handle[] pPhysicalDevices;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("physicalDeviceCount"),
			PADDING,
			POINTER.withName("pPhysicalDevices")
		);
	}
}
