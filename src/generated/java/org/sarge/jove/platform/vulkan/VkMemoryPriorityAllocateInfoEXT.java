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
	"priority"
})
public class VkMemoryPriorityAllocateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkMemoryPriorityAllocateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkMemoryPriorityAllocateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_MEMORY_PRIORITY_ALLOCATE_INFO_EXT;
	public Pointer pNext;
	public float priority;
}
