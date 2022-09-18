package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

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
public class VkHdrMetadataEXT extends VulkanStructure {
	public VkStructureType sType = VkStructureType.HDR_METADATA_EXT;
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
