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
public class VkBufferCopy implements NativeStructure {
	public long srcOffset;
	public long dstOffset;
	public long size;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_LONG.withName("srcOffset"),
			JAVA_LONG.withName("dstOffset"),
			JAVA_LONG.withName("size")
		);
	}
}
