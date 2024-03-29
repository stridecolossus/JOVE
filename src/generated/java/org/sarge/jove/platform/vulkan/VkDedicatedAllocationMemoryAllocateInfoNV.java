package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
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
	public VkStructureType sType = VkStructureType.DEDICATED_ALLOCATION_MEMORY_ALLOCATE_INFO_NV;
	public Pointer pNext;
	public Pointer image;
	public Pointer buffer;
}
