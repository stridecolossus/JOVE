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
public class VkViewportSwizzleNV implements NativeStructure {
	public VkViewportCoordinateSwizzleNV x;
	public VkViewportCoordinateSwizzleNV y;
	public VkViewportCoordinateSwizzleNV z;
	public VkViewportCoordinateSwizzleNV w;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("x"),
			JAVA_INT.withName("y"),
			JAVA_INT.withName("z"),
			JAVA_INT.withName("w")
		);
	}
}
