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
	"attachmentInitialSampleLocationsCount",
	"pAttachmentInitialSampleLocations",
	"postSubpassSampleLocationsCount",
	"pPostSubpassSampleLocations"
})
public class VkRenderPassSampleLocationsBeginInfoEXT extends Structure {
	public static class ByValue extends VkRenderPassSampleLocationsBeginInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkRenderPassSampleLocationsBeginInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_RENDER_PASS_SAMPLE_LOCATIONS_BEGIN_INFO_EXT.value();
	public Pointer pNext;
	public int attachmentInitialSampleLocationsCount;
	public VkAttachmentSampleLocationsEXT.ByReference pAttachmentInitialSampleLocations;
	public int postSubpassSampleLocationsCount;
	public VkSubpassSampleLocationsEXT.ByReference pPostSubpassSampleLocations;
}
