package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
	"srcQueueFamilyIndex",
	"dstQueueFamilyIndex",
	"buffer",
	"offset",
	"size"
})
public class VkBufferMemoryBarrier extends VulkanStructure {
	public static class ByValue extends VkBufferMemoryBarrier implements Structure.ByValue { }
	public static class ByReference extends VkBufferMemoryBarrier implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.BUFFER_MEMORY_BARRIER;
	public Pointer pNext;
	public VkAccess srcAccessMask;
	public VkAccess dstAccessMask;
	public int srcQueueFamilyIndex;
	public int dstQueueFamilyIndex;
	public Pointer buffer;
	public long offset;
	public long size;
}
