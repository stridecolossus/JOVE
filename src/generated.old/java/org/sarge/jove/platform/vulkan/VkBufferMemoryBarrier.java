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
	"srcQueueFamilyIndex",
	"dstQueueFamilyIndex",
	"buffer",
	"offset",
	"size"
})
public class VkBufferMemoryBarrier extends Structure {
	public static class ByValue extends VkBufferMemoryBarrier implements Structure.ByValue { }
	public static class ByReference extends VkBufferMemoryBarrier implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_BUFFER_MEMORY_BARRIER.value();
	public Pointer pNext;
	public int srcAccessMask;
	public int dstAccessMask;
	public int srcQueueFamilyIndex;
	public int dstQueueFamilyIndex;
	public long buffer;
	public long offset;
	public long size;
}
