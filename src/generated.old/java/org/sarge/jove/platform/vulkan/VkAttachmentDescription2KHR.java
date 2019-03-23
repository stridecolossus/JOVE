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
	"flags",
	"format",
	"samples",
	"loadOp",
	"storeOp",
	"stencilLoadOp",
	"stencilStoreOp",
	"initialLayout",
	"finalLayout"
})
public class VkAttachmentDescription2KHR extends Structure {
	public static class ByValue extends VkAttachmentDescription2KHR implements Structure.ByValue { }
	public static class ByReference extends VkAttachmentDescription2KHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_ATTACHMENT_DESCRIPTION_2_KHR.value();
	public Pointer pNext;
	public int flags;
	public int format;
	public int samples;
	public int loadOp;
	public int storeOp;
	public int stencilLoadOp;
	public int stencilStoreOp;
	public int initialLayout;
	public int finalLayout;
}
