package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkOffset3D implements NativeStructure {
	public int x;
	public int y;
	public int z;
	@Override

	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("x"),
				JAVA_INT.withName("y"),
				JAVA_INT.withName("z"),
				PADDING
		);
	}
}
