package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"bufferOffset",
	"bufferRowLength",
	"bufferImageHeight",
	"imageSubresource",
	"imageOffset",
	"imageExtent"
})
public class VkBufferImageCopy extends VulkanStructure {
	public static class ByValue extends VkBufferImageCopy implements Structure.ByValue { }
	public static class ByReference extends VkBufferImageCopy implements Structure.ByReference { }
	
	public long bufferOffset;
	public int bufferRowLength;
	public int bufferImageHeight;
	public VkImageSubresourceLayers imageSubresource;
	public VkOffset3D imageOffset;
	public VkExtent3D imageExtent;
}
