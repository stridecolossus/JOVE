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
	"sampleLocationsEnable",
	"sampleLocationsInfo"
})
public class VkPipelineSampleLocationsStateCreateInfoEXT extends Structure {
	public static class ByValue extends VkPipelineSampleLocationsStateCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPipelineSampleLocationsStateCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_SAMPLE_LOCATIONS_STATE_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public boolean sampleLocationsEnable;
	public VkSampleLocationsInfoEXT sampleLocationsInfo;
}
