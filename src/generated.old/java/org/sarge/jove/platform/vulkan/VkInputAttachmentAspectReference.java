package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"subpass",
	"inputAttachmentIndex",
	"aspectMask"
})
public class VkInputAttachmentAspectReference extends Structure {
	public static class ByValue extends VkInputAttachmentAspectReference implements Structure.ByValue { }
	public static class ByReference extends VkInputAttachmentAspectReference implements Structure.ByReference { }
	
	public int subpass;
	public int inputAttachmentIndex;
	public int aspectMask;
}
