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
	"inlineUniformBlock",
	"descriptorBindingInlineUniformBlockUpdateAfterBind"
})
public class VkPhysicalDeviceInlineUniformBlockFeaturesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceInlineUniformBlockFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceInlineUniformBlockFeaturesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_INLINE_UNIFORM_BLOCK_FEATURES_EXT.value();
	public Pointer pNext;
	public boolean inlineUniformBlock;
	public boolean descriptorBindingInlineUniformBlockUpdateAfterBind;
}
