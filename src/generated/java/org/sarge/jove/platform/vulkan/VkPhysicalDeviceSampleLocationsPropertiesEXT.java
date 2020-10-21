package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

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
public class VkPhysicalDeviceSampleLocationsPropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceSampleLocationsPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceSampleLocationsPropertiesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SAMPLE_LOCATIONS_PROPERTIES_EXT;
	public Pointer pNext;
	public VkSampleCountFlags sampleLocationSampleCounts;
	public VkExtent2D maxSampleLocationGridSize;
	public float[] sampleLocationCoordinateRange = new float[2];
	public int sampleLocationSubPixelBits;
	public VulkanBoolean variableSampleLocations;
}
