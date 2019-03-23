package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

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
public class VkImageMemoryBarrier extends Structure {
	public static class ByValue extends VkImageMemoryBarrier implements Structure.ByValue { }
	public static class ByReference extends VkImageMemoryBarrier implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER.value();
	public Pointer pNext;
	public int srcAccessMask;
	public int dstAccessMask;
	public int oldLayout;
	public int newLayout;
	public int srcQueueFamilyIndex;
	public int dstQueueFamilyIndex;
	public long image;
	public VkImageSubresourceRange subresourceRange;
}
