package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.VulkanBoolean;
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
	"sampleLocationsEnable",
	"sampleLocationsInfo"
})
public class VkPipelineSampleLocationsStateCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkPipelineSampleLocationsStateCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPipelineSampleLocationsStateCreateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_SAMPLE_LOCATIONS_STATE_CREATE_INFO_EXT;
	public Pointer pNext;
	public VulkanBoolean sampleLocationsEnable;
	public VkSampleLocationsInfoEXT sampleLocationsInfo;
}
