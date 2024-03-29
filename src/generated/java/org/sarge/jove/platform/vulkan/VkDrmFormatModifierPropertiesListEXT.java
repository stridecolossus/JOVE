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
	"drmFormatModifierCount",
	"pDrmFormatModifierProperties"
})
public class VkDrmFormatModifierPropertiesListEXT extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DRM_FORMAT_MODIFIER_PROPERTIES_LIST_EXT;
	public Pointer pNext;
	public int drmFormatModifierCount;
	public Pointer pDrmFormatModifierProperties;
}
