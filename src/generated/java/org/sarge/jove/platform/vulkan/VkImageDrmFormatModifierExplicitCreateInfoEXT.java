package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"drmFormatModifier",
	"drmFormatModifierPlaneCount",
	"pPlaneLayouts"
})
public class VkImageDrmFormatModifierExplicitCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkImageDrmFormatModifierExplicitCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkImageDrmFormatModifierExplicitCreateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.IMAGE_DRM_FORMAT_MODIFIER_EXPLICIT_CREATE_INFO_EXT;
	public Pointer pNext;
	public long drmFormatModifier;
	public int drmFormatModifierPlaneCount;
	public Pointer pPlaneLayouts;
}
