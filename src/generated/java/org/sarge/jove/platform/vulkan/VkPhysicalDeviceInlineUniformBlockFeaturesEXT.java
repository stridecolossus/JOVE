package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.*;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"inlineUniformBlock",
	"descriptorBindingInlineUniformBlockUpdateAfterBind"
})
public class VkPhysicalDeviceInlineUniformBlockFeaturesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceInlineUniformBlockFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceInlineUniformBlockFeaturesEXT implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_INLINE_UNIFORM_BLOCK_FEATURES_EXT;
	public Pointer pNext;
	public boolean inlineUniformBlock;
	public boolean descriptorBindingInlineUniformBlockUpdateAfterBind;
}
