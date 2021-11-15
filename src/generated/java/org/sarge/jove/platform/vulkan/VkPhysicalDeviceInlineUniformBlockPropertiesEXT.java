package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
	"maxInlineUniformBlockSize",
	"maxPerStageDescriptorInlineUniformBlocks",
	"maxPerStageDescriptorUpdateAfterBindInlineUniformBlocks",
	"maxDescriptorSetInlineUniformBlocks",
	"maxDescriptorSetUpdateAfterBindInlineUniformBlocks"
})
public class VkPhysicalDeviceInlineUniformBlockPropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceInlineUniformBlockPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceInlineUniformBlockPropertiesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_INLINE_UNIFORM_BLOCK_PROPERTIES_EXT;
	public Pointer pNext;
	public int maxInlineUniformBlockSize;
	public int maxPerStageDescriptorInlineUniformBlocks;
	public int maxPerStageDescriptorUpdateAfterBindInlineUniformBlocks;
	public int maxDescriptorSetInlineUniformBlocks;
	public int maxDescriptorSetUpdateAfterBindInlineUniformBlocks;
}
