package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
	"drmFormatModifier"
})
public class VkImageDrmFormatModifierPropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkImageDrmFormatModifierPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkImageDrmFormatModifierPropertiesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.IMAGE_DRM_FORMAT_MODIFIER_PROPERTIES_EXT;
	public Pointer pNext;
	public long drmFormatModifier;
}
