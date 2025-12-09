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
public class VkClearRect implements NativeStructure {
	public VkRect2D rect;
	public int baseArrayLayer;
	public int layerCount;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			MemoryLayout.structLayout(
				MemoryLayout.structLayout(
					JAVA_INT.withName("x"),
					JAVA_INT.withName("y")
				).withName("offset"),
				MemoryLayout.structLayout(
					JAVA_INT.withName("width"),
					JAVA_INT.withName("height")
				).withName("extent")
			).withName("rect"),
			JAVA_INT.withName("baseArrayLayer"),
			JAVA_INT.withName("layerCount")
		);
	}
}
