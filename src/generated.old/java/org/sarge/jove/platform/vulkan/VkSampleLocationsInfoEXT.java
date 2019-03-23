package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"sampleLocationsPerPixel",
	"sampleLocationGridSize",
	"sampleLocationsCount",
	"pSampleLocations"
})
public class VkSampleLocationsInfoEXT extends Structure {
	public static class ByValue extends VkSampleLocationsInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkSampleLocationsInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SAMPLE_LOCATIONS_INFO_EXT.value();
	public Pointer pNext;
	public int sampleLocationsPerPixel;
	public VkExtent2D sampleLocationGridSize;
	public int sampleLocationsCount;
	public VkSampleLocationEXT.ByReference pSampleLocations;
}
