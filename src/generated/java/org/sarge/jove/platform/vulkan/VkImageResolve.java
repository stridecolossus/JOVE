package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"srcSubresource",
	"srcOffset",
	"dstSubresource",
	"dstOffset",
	"extent"
})
public class VkImageResolve extends VulkanStructure {
	public static class ByValue extends VkImageResolve implements Structure.ByValue { }
	public static class ByReference extends VkImageResolve implements Structure.ByReference { }
	
	public VkImageSubresourceLayers srcSubresource;
	public VkOffset3D srcOffset;
	public VkImageSubresourceLayers dstSubresource;
	public VkOffset3D dstOffset;
	public VkExtent3D extent;
}
