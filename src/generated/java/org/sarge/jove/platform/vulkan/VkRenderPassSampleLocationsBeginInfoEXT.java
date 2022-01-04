package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"attachmentInitialSampleLocationsCount",
	"pAttachmentInitialSampleLocations",
	"postSubpassSampleLocationsCount",
	"pPostSubpassSampleLocations"
})
public class VkRenderPassSampleLocationsBeginInfoEXT extends VulkanStructure {
	public static class ByValue extends VkRenderPassSampleLocationsBeginInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkRenderPassSampleLocationsBeginInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.RENDER_PASS_SAMPLE_LOCATIONS_BEGIN_INFO_EXT;
	public Pointer pNext;
	public int attachmentInitialSampleLocationsCount;
	public Pointer pAttachmentInitialSampleLocations;
	public int postSubpassSampleLocationsCount;
	public Pointer pPostSubpassSampleLocations;
}
