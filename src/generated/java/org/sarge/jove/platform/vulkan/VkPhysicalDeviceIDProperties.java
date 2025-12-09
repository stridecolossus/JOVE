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
public class VkPhysicalDeviceIDProperties implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public byte[] deviceUUID;
	public byte[] driverUUID;
	public byte[] deviceLUID;
	public int deviceNodeMask;
	public boolean deviceLUIDValid;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			MemoryLayout.sequenceLayout(16, JAVA_BYTE).withName("deviceUUID"),
			MemoryLayout.sequenceLayout(16, JAVA_BYTE).withName("driverUUID"),
			MemoryLayout.sequenceLayout(8, JAVA_BYTE).withName("deviceLUID"),
			JAVA_INT.withName("deviceNodeMask"),
			JAVA_INT.withName("deviceLUIDValid")
		);
	}
}
