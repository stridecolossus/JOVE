package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.BitMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkMemoryType extends NativeStructure {
	public BitMask<VkMemoryProperty> propertyFlags;
	public int heapIndex;

	@Override
	protected StructLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("propertyFlags"),
				JAVA_INT.withName("heapIndex")
		);
	}
}
