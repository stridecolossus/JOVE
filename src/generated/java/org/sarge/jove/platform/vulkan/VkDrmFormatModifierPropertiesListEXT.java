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
	"drmFormatModifierCount",
	"pDrmFormatModifierProperties"
})
public class VkDrmFormatModifierPropertiesListEXT extends VulkanStructure {
	public static class ByValue extends VkDrmFormatModifierPropertiesListEXT implements Structure.ByValue { }
	public static class ByReference extends VkDrmFormatModifierPropertiesListEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.DRM_FORMAT_MODIFIER_PROPERTIES_LIST_EXT;
	public Pointer pNext;
	public int drmFormatModifierCount;
	public Pointer pDrmFormatModifierProperties;
}
