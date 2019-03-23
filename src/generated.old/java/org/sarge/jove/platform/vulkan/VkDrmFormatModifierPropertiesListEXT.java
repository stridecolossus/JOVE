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
	"pDrmFormatModifierProperties"
})
public class VkDrmFormatModifierPropertiesListEXT extends Structure {
	public static class ByValue extends VkDrmFormatModifierPropertiesListEXT implements Structure.ByValue { }
	public static class ByReference extends VkDrmFormatModifierPropertiesListEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DRM_FORMAT_MODIFIER_PROPERTIES_LIST_EXT.value();
	public Pointer pNext;
	public int drmFormatModifierCount;
	public VkDrmFormatModifierPropertiesEXT.ByReference pDrmFormatModifierProperties;
}
