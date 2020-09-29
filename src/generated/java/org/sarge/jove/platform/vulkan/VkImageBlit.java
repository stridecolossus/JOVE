package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
public class VkImageBlit extends VulkanStructure {
	public static class ByValue extends VkImageBlit implements Structure.ByValue { }
	public static class ByReference extends VkImageBlit implements Structure.ByReference { }
	
	public VkImageSubresourceLayers srcSubresource;
	public VkOffset3D[] srcOffsets = new VkOffset3D[2];
	public VkImageSubresourceLayers dstSubresource;
	public VkOffset3D[] dstOffsets = new VkOffset3D[2];
}
