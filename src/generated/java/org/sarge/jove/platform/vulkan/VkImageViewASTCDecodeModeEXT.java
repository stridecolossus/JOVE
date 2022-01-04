package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"decodeMode"
})
public class VkImageViewASTCDecodeModeEXT extends VulkanStructure {
	public static class ByValue extends VkImageViewASTCDecodeModeEXT implements Structure.ByValue { }
	public static class ByReference extends VkImageViewASTCDecodeModeEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.IMAGE_VIEW_ASTC_DECODE_MODE_EXT;
	public Pointer pNext;
	public VkFormat decodeMode;
}
