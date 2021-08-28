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
	"dataSize",
	"pData"
})
public class VkWriteDescriptorSetInlineUniformBlockEXT extends VulkanStructure {
	public static class ByValue extends VkWriteDescriptorSetInlineUniformBlockEXT implements Structure.ByValue { }
	public static class ByReference extends VkWriteDescriptorSetInlineUniformBlockEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.WRITE_DESCRIPTOR_SET_INLINE_UNIFORM_BLOCK_EXT;
	public Pointer pNext;
	public int dataSize;
	public Pointer pData;
}
