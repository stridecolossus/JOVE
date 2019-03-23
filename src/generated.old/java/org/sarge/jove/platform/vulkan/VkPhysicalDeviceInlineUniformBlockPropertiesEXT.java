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
	"maxInlineUniformBlockSize",
	"maxPerStageDescriptorInlineUniformBlocks",
	"maxPerStageDescriptorUpdateAfterBindInlineUniformBlocks",
	"maxDescriptorSetInlineUniformBlocks",
	"maxDescriptorSetUpdateAfterBindInlineUniformBlocks"
})
public class VkPhysicalDeviceInlineUniformBlockPropertiesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceInlineUniformBlockPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceInlineUniformBlockPropertiesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_INLINE_UNIFORM_BLOCK_PROPERTIES_EXT.value();
	public Pointer pNext;
	public int maxInlineUniformBlockSize;
	public int maxPerStageDescriptorInlineUniformBlocks;
	public int maxPerStageDescriptorUpdateAfterBindInlineUniformBlocks;
	public int maxDescriptorSetInlineUniformBlocks;
	public int maxDescriptorSetUpdateAfterBindInlineUniformBlocks;
}
