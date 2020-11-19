package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"dstAccessMask"
})
public class VkMemoryBarrier extends VulkanStructure {
	public static class ByValue extends VkMemoryBarrier implements Structure.ByValue { }
	public static class ByReference extends VkMemoryBarrier implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_MEMORY_BARRIER;
	public Pointer pNext;
	public VkAccessFlag srcAccessMask;
	public VkAccessFlag dstAccessMask;
}
