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
	"fragmentDensityMapAttachment"
})
public class VkRenderPassFragmentDensityMapCreateInfoEXT extends Structure {
	public static class ByValue extends VkRenderPassFragmentDensityMapCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkRenderPassFragmentDensityMapCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_RENDER_PASS_FRAGMENT_DENSITY_MAP_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public VkAttachmentReference fragmentDensityMapAttachment;
}
