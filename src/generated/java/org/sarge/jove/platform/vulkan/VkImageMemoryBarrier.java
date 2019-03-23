package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import com.sun.jna.Pointer;

import com.sun.jna.Structure;
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
	public static class ByValue extends VkImageMemoryBarrier implements Structure.ByValue { }
	public static class ByReference extends VkImageMemoryBarrier implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
	public Pointer pNext;
	public VkAccessFlags srcAccessMask;
	public VkAccessFlags dstAccessMask;
	public VkImageLayout oldLayout;
	public VkImageLayout newLayout;
	public int srcQueueFamilyIndex;
	public int dstQueueFamilyIndex;
	public Pointer image;
	public VkImageSubresourceRange subresourceRange;
}
