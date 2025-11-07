package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkMemoryRequirements implements NativeStructure {
	public long size;
	public long alignment;
	public int memoryTypeBits;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_LONG.withName("size"),
				JAVA_LONG.withName("alignment"),
				JAVA_INT.withName("memoryTypeBits"),
				PADDING
		);
	}
}
