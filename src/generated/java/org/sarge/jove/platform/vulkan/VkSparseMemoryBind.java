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
public class VkSparseMemoryBind implements NativeStructure {
	public long resourceOffset;
	public long size;
	public Handle memory;
	public long memoryOffset;
	public EnumMask<VkSparseMemoryBindFlags> flags;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_LONG.withName("resourceOffset"),
			JAVA_LONG.withName("size"),
			POINTER.withName("memory"),
			JAVA_LONG.withName("memoryOffset"),
			JAVA_INT.withName("flags")
		);
	}
}
