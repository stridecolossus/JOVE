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
public class VkSampleLocationsInfoEXT implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public EnumMask<VkSampleCountFlags> sampleLocationsPerPixel;
	public VkExtent2D sampleLocationGridSize;
	public int sampleLocationsCount;
	public VkSampleLocationEXT[] pSampleLocations;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("sampleLocationsPerPixel"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("width"),
				JAVA_INT.withName("height")
			).withName("sampleLocationGridSize"),
			JAVA_INT.withName("sampleLocationsCount"),
			POINTER.withName("pSampleLocations")
		);
	}
}
