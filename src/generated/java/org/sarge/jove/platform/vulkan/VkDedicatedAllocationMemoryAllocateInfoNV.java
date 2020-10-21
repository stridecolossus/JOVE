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
	"image",
	"buffer"
})
public class VkDedicatedAllocationMemoryAllocateInfoNV extends VulkanStructure {
	public static class ByValue extends VkDedicatedAllocationMemoryAllocateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkDedicatedAllocationMemoryAllocateInfoNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DEDICATED_ALLOCATION_MEMORY_ALLOCATE_INFO_NV;
	public Pointer pNext;
	public Pointer image;
	public Pointer buffer;
}
