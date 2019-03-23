package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"aspectMask",
	"mipLevel",
	"arrayLayer"
})
public class VkImageSubresource extends Structure {
	public static class ByValue extends VkImageSubresource implements Structure.ByValue { }
	public static class ByReference extends VkImageSubresource implements Structure.ByReference { }
	
	public int aspectMask;
	public int mipLevel;
	public int arrayLayer;
}
