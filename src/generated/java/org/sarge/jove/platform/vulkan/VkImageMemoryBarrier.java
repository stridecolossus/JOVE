package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"srcAccessMask",
	"dstAccessMask",
	"oldLayout",
	"newLayout",
	"srcQueueFamilyIndex",
	"dstQueueFamilyIndex",
	"image",
	"subresourceRange"
})
public class VkImageMemoryBarrier extends VulkanStructure {
	public VkStructureType sType = VkStructureType.IMAGE_MEMORY_BARRIER;
	public Pointer pNext;
	public int srcAccessMask;
	public int dstAccessMask;
	public VkImageLayout oldLayout;
	public VkImageLayout newLayout;
	public int srcQueueFamilyIndex;
	public int dstQueueFamilyIndex;
	public Handle image;
	public VkImageSubresourceRange subresourceRange;
}
