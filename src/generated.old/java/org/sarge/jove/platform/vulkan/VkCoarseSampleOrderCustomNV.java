package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"shadingRate",
	"sampleCount",
	"sampleLocationCount",
	"pSampleLocations"
})
public class VkCoarseSampleOrderCustomNV extends Structure {
	public static class ByValue extends VkCoarseSampleOrderCustomNV implements Structure.ByValue { }
	public static class ByReference extends VkCoarseSampleOrderCustomNV implements Structure.ByReference { }
	
	public int shadingRate;
	public int sampleCount;
	public int sampleLocationCount;
	public VkCoarseSampleLocationNV.ByReference pSampleLocations;
}
