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
public class VkPhysicalDeviceSampleLocationsPropertiesEXT implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public EnumMask<VkSampleCountFlags> sampleLocationSampleCounts;
	public VkExtent2D maxSampleLocationGridSize;
	public float[] sampleLocationCoordinateRange;
	public int sampleLocationSubPixelBits;
	public boolean variableSampleLocations;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("sampleLocationSampleCounts"),
			PADDING,
			MemoryLayout.structLayout(
				JAVA_INT.withName("width"),
				JAVA_INT.withName("height")
			).withName("maxSampleLocationGridSize"),
			MemoryLayout.sequenceLayout(2, JAVA_FLOAT).withName("sampleLocationCoordinateRange"),
			JAVA_INT.withName("sampleLocationSubPixelBits"),
			JAVA_INT.withName("variableSampleLocations")
		);
	}
}
