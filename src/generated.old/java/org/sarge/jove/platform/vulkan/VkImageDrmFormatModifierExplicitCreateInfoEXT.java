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
	"drmFormatModifier",
	"drmFormatModifierPlaneCount",
	"pPlaneLayouts"
})
public class VkImageDrmFormatModifierExplicitCreateInfoEXT extends Structure {
	public static class ByValue extends VkImageDrmFormatModifierExplicitCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkImageDrmFormatModifierExplicitCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_IMAGE_DRM_FORMAT_MODIFIER_EXPLICIT_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public long drmFormatModifier;
	public int drmFormatModifierPlaneCount;
	public VkSubresourceLayout.ByReference pPlaneLayouts;
}
