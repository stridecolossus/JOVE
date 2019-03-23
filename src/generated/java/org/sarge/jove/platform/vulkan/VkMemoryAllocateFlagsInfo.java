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
	"flags",
	"deviceMask"
})
public class VkMemoryAllocateFlagsInfo extends VulkanStructure {
	public static class ByValue extends VkMemoryAllocateFlagsInfo implements Structure.ByValue { }
	public static class ByReference extends VkMemoryAllocateFlagsInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_FLAGS_INFO;
	public Pointer pNext;
	public int flags;
	public int deviceMask;
}
