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
	"drmFormatModifierCount",
	"pDrmFormatModifiers"
})
public class VkImageDrmFormatModifierListCreateInfoEXT extends Structure {
	public static class ByValue extends VkImageDrmFormatModifierListCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkImageDrmFormatModifierListCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_IMAGE_DRM_FORMAT_MODIFIER_LIST_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public int drmFormatModifierCount;
	public long pDrmFormatModifiers;
}
