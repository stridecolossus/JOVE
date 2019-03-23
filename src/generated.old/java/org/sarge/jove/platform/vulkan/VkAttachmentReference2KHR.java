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
	"attachment",
	"layout",
	"aspectMask"
})
public class VkAttachmentReference2KHR extends Structure {
	public static class ByValue extends VkAttachmentReference2KHR implements Structure.ByValue { }
	public static class ByReference extends VkAttachmentReference2KHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_ATTACHMENT_REFERENCE_2_KHR.value();
	public Pointer pNext;
	public int attachment;
	public int layout;
	public int aspectMask;
}
