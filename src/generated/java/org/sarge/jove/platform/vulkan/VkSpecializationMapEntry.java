package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkSpecializationMapEntry implements NativeStructure {
	public int constantID;
	public int offset;
	public long size;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("constantID"),
				JAVA_INT.withName("offset"),
				JAVA_INT.withName("size")
		);
	}
}
