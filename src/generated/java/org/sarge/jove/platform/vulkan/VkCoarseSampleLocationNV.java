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
public class VkCoarseSampleLocationNV implements NativeStructure {
	public int pixelX;
	public int pixelY;
	public int sample;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("pixelX"),
			JAVA_INT.withName("pixelY"),
			JAVA_INT.withName("sample")
		);
	}
}
