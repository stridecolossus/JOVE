package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"attachmentIndex",
	"sampleLocationsInfo"
})
public class VkAttachmentSampleLocationsEXT extends VulkanStructure {
	public int attachmentIndex;
	public VkSampleLocationsInfoEXT sampleLocationsInfo;
}
