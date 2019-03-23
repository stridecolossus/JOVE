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
	"sampleLocationSampleCounts",
	"maxSampleLocationGridSize",
	"sampleLocationCoordinateRange",
	"sampleLocationSubPixelBits",
	"variableSampleLocations"
})
public class VkPhysicalDeviceSampleLocationsPropertiesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceSampleLocationsPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceSampleLocationsPropertiesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SAMPLE_LOCATIONS_PROPERTIES_EXT.value();
	public Pointer pNext;
	public int sampleLocationSampleCounts;
	public VkExtent2D maxSampleLocationGridSize;
	public final float[] sampleLocationCoordinateRange = new float[2];
	public int sampleLocationSubPixelBits;
	public boolean variableSampleLocations;
}
