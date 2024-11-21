package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.lib.NativeStructure;
import org.sarge.jove.util.BitMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkQueueFamilyProperties extends NativeStructure {
	public BitMask<VkQueueFlag> queueFlags;
	public int queueCount;
	public int timestampValidBits;
//	public VkExtent3D minImageTransferGranularity;
	public int width;
	public int height;
	public int depth;

	@Override
	protected StructLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("queueFlags"),
				JAVA_INT.withName("queueCount"),
				JAVA_INT.withName("timestampValidBits"),
//				MemoryLayout.structLayout(
		            JAVA_INT.withName("width"),
		            JAVA_INT.withName("height"),
		            JAVA_INT.withName("depth")
//		        ) // .withName("minImageTransferGranularity")
		);
	}
}
