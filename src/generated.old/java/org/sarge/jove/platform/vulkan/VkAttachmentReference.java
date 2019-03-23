package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"attachment",
	"layout"
})
public class VkAttachmentReference extends Structure {
	public static class ByValue extends VkAttachmentReference implements Structure.ByValue { }
	public static class ByReference extends VkAttachmentReference implements Structure.ByReference { }
	
	public int attachment;
	public int layout;
}
