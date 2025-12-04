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
public class VkDisplayPresentInfoKHR implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public VkRect2D srcRect;
	public VkRect2D dstRect;
	public boolean persistent;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			MemoryLayout.structLayout(
				MemoryLayout.structLayout(
					JAVA_INT.withName("x"),
					JAVA_INT.withName("y")
				).withName("offset"),
				MemoryLayout.structLayout(
					JAVA_INT.withName("width"),
					JAVA_INT.withName("height")
				).withName("extent")
			).withName("srcRect"),
			MemoryLayout.structLayout(
				MemoryLayout.structLayout(
					JAVA_INT.withName("x"),
					JAVA_INT.withName("y")
				).withName("offset"),
				MemoryLayout.structLayout(
					JAVA_INT.withName("width"),
					JAVA_INT.withName("height")
				).withName("extent")
			).withName("dstRect"),
			JAVA_INT.withName("persistent")
		);
	}
}
