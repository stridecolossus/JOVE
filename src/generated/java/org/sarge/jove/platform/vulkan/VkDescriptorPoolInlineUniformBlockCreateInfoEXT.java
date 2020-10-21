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
	"maxInlineUniformBlockBindings"
})
public class VkDescriptorPoolInlineUniformBlockCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkDescriptorPoolInlineUniformBlockCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorPoolInlineUniformBlockCreateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_INLINE_UNIFORM_BLOCK_CREATE_INFO_EXT;
	public Pointer pNext;
	public int maxInlineUniformBlockBindings;
}
