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
	"allocationSize",
	"memoryTypeIndex"
})
public class VkMemoryAllocateInfo extends VulkanStructure {
	public static class ByValue extends VkMemoryAllocateInfo implements Structure.ByValue { }
	public static class ByReference extends VkMemoryAllocateInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
	public Pointer pNext;
	public long allocationSize;
	public int memoryTypeIndex;
}
