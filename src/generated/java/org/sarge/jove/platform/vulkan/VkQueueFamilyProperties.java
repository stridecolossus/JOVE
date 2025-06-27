package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkQueueFamilyProperties implements NativeStructure {
	public EnumMask<VkQueueFlag> queueFlags;
	public int queueCount;
	public int timestampValidBits;
	public VkExtent3D minImageTransferGranularity; // TODO - constructor? (no)

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("queueFlags"),
				JAVA_INT.withName("queueCount"),
				JAVA_INT.withName("timestampValidBits"),
				MemoryLayout.structLayout(
		            JAVA_INT.withName("width"),
		            JAVA_INT.withName("height"),
		            JAVA_INT.withName("depth")
				).withName("minImageTransferGranularity")
		);
	}
}
