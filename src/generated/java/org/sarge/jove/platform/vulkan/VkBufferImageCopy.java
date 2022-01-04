package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	public long bufferOffset;
	public int bufferRowLength;
	public int bufferImageHeight;
	public VkImageSubresourceLayers imageSubresource;
	public VkOffset3D imageOffset;
	public VkExtent3D imageExtent;
}
