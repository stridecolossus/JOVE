package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
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
	public VkStructureType sType = VkStructureType.SAMPLE_LOCATIONS_INFO_EXT;
	public Pointer pNext;
	public VkSampleCount sampleLocationsPerPixel;
	public VkExtent2D sampleLocationGridSize;
	public int sampleLocationsCount;
	public Pointer pSampleLocations;
}
