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
	"displayPrimaryRed",
	"displayPrimaryGreen",
	"displayPrimaryBlue",
	"whitePoint",
	"maxLuminance",
	"minLuminance",
	"maxContentLightLevel",
	"maxFrameAverageLightLevel"
})
public class VkHdrMetadataEXT extends Structure {
	public static class ByValue extends VkHdrMetadataEXT implements Structure.ByValue { }
	public static class ByReference extends VkHdrMetadataEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_HDR_METADATA_EXT.value();
	public Pointer pNext;
	public VkXYColorEXT displayPrimaryRed;
	public VkXYColorEXT displayPrimaryGreen;
	public VkXYColorEXT displayPrimaryBlue;
	public VkXYColorEXT whitePoint;
	public float maxLuminance;
	public float minLuminance;
	public float maxContentLightLevel;
	public float maxFrameAverageLightLevel;
}
