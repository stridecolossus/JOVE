package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"attachmentIndex",
	"sampleLocationsInfo"
})
public class VkAttachmentSampleLocationsEXT extends Structure {
	public static class ByValue extends VkAttachmentSampleLocationsEXT implements Structure.ByValue { }
	public static class ByReference extends VkAttachmentSampleLocationsEXT implements Structure.ByReference { }
	
	public int attachmentIndex;
	public VkSampleLocationsInfoEXT sampleLocationsInfo;
}
