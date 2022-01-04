package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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

	public VkStructureType sType = VkStructureType.MEMORY_BARRIER;
	public Pointer pNext;
	public VkAccess srcAccessMask;
	public VkAccess dstAccessMask;
}
