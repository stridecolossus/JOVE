package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitField;

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
	"srcQueueFamilyIndex",
	"dstQueueFamilyIndex",
	"buffer",
	"offset",
	"size"
})
public class VkBufferMemoryBarrier extends VulkanStructure {
	public VkStructureType sType = VkStructureType.BUFFER_MEMORY_BARRIER;
	public Pointer pNext;
	public BitField<VkAccess> srcAccessMask;
	public BitField<VkAccess> dstAccessMask;
	public int srcQueueFamilyIndex;
	public int dstQueueFamilyIndex;
	public Handle buffer;
	public long offset;
	public long size;
}
