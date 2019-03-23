package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"srcSubresource",
	"srcOffsets",
	"dstSubresource",
	"dstOffsets"
})
public class VkImageBlit extends Structure {
	public static class ByValue extends VkImageBlit implements Structure.ByValue { }
	public static class ByReference extends VkImageBlit implements Structure.ByReference { }
	
	public VkImageSubresourceLayers srcSubresource;
	public final VkOffset3D[] srcOffsets = new VkOffset3D[2];
	public VkImageSubresourceLayers dstSubresource;
	public final VkOffset3D[] dstOffsets = new VkOffset3D[2];
}
