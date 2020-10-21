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
	"drmFormatModifierCount",
	"pDrmFormatModifiers"
})
public class VkImageDrmFormatModifierListCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkImageDrmFormatModifierListCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkImageDrmFormatModifierListCreateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_IMAGE_DRM_FORMAT_MODIFIER_LIST_CREATE_INFO_EXT;
	public Pointer pNext;
	public int drmFormatModifierCount;
	public Pointer pDrmFormatModifiers;
}
