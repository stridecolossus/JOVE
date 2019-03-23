package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
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
	"fragmentDensityMapAttachment"
})
public class VkRenderPassFragmentDensityMapCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkRenderPassFragmentDensityMapCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkRenderPassFragmentDensityMapCreateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_RENDER_PASS_FRAGMENT_DENSITY_MAP_CREATE_INFO_EXT;
	public Pointer pNext;
	public VkAttachmentReference fragmentDensityMapAttachment;
}
