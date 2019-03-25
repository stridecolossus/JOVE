package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
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
	"sampleLocationsPerPixel",
	"sampleLocationGridSize",
	"sampleLocationsCount",
	"pSampleLocations"
})
public class VkSampleLocationsInfoEXT extends VulkanStructure {
	public static class ByValue extends VkSampleLocationsInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkSampleLocationsInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_SAMPLE_LOCATIONS_INFO_EXT;
	public Pointer pNext;
	public VkSampleCountFlagBits sampleLocationsPerPixel;
	public VkExtent2D sampleLocationGridSize;
	public int sampleLocationsCount;
	public Pointer pSampleLocations;
}