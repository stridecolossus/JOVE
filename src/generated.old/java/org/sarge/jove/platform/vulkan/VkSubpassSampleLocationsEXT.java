package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"subpassIndex",
	"sampleLocationsInfo"
})
public class VkSubpassSampleLocationsEXT extends Structure {
	public static class ByValue extends VkSubpassSampleLocationsEXT implements Structure.ByValue { }
	public static class ByReference extends VkSubpassSampleLocationsEXT implements Structure.ByReference { }
	
	public int subpassIndex;
	public VkSampleLocationsInfoEXT sampleLocationsInfo;
}
