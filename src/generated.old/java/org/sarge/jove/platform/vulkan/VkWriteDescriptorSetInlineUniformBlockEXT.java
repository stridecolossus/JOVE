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
	"dataSize",
	"pData"
})
public class VkWriteDescriptorSetInlineUniformBlockEXT extends Structure {
	public static class ByValue extends VkWriteDescriptorSetInlineUniformBlockEXT implements Structure.ByValue { }
	public static class ByReference extends VkWriteDescriptorSetInlineUniformBlockEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET_INLINE_UNIFORM_BLOCK_EXT.value();
	public Pointer pNext;
	public int dataSize;
	public Pointer pData;
}
