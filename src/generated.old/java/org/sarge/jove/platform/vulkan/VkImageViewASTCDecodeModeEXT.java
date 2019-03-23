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
	"decodeMode"
})
public class VkImageViewASTCDecodeModeEXT extends Structure {
	public static class ByValue extends VkImageViewASTCDecodeModeEXT implements Structure.ByValue { }
	public static class ByReference extends VkImageViewASTCDecodeModeEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_IMAGE_VIEW_ASTC_DECODE_MODE_EXT.value();
	public Pointer pNext;
	public int decodeMode;
}
