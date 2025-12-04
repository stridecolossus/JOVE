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
public class VkCoarseSampleOrderCustomNV implements NativeStructure {
	public VkShadingRatePaletteEntryNV shadingRate;
	public int sampleCount;
	public int sampleLocationCount;
	public VkCoarseSampleLocationNV[] pSampleLocations;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("shadingRate"),
			JAVA_INT.withName("sampleCount"),
			JAVA_INT.withName("sampleLocationCount"),
			PADDING,
			POINTER.withName("pSampleLocations")
		);
	}
}
