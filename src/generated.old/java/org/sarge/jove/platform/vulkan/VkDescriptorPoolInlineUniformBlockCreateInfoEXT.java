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
	"maxInlineUniformBlockBindings"
})
public class VkDescriptorPoolInlineUniformBlockCreateInfoEXT extends Structure {
	public static class ByValue extends VkDescriptorPoolInlineUniformBlockCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorPoolInlineUniformBlockCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_INLINE_UNIFORM_BLOCK_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public int maxInlineUniformBlockBindings;
}
