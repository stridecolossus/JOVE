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
	"maxInlineUniformBlockBindings"
})
public class VkDescriptorPoolInlineUniformBlockCreateInfoEXT extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DESCRIPTOR_POOL_INLINE_UNIFORM_BLOCK_CREATE_INFO_EXT;
	public Pointer pNext;
	public int maxInlineUniformBlockBindings;
}
