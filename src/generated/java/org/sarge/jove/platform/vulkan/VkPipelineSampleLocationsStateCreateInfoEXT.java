package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.*;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"sampleLocationsEnable",
	"sampleLocationsInfo"
})
public class VkPipelineSampleLocationsStateCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkPipelineSampleLocationsStateCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPipelineSampleLocationsStateCreateInfoEXT implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PIPELINE_SAMPLE_LOCATIONS_STATE_CREATE_INFO_EXT;
	public Pointer pNext;
	public boolean sampleLocationsEnable;
	public VkSampleLocationsInfoEXT sampleLocationsInfo;
}
