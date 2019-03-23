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
	"image",
	"buffer"
})
public class VkDedicatedAllocationMemoryAllocateInfoNV extends Structure {
	public static class ByValue extends VkDedicatedAllocationMemoryAllocateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkDedicatedAllocationMemoryAllocateInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEDICATED_ALLOCATION_MEMORY_ALLOCATE_INFO_NV.value();
	public Pointer pNext;
	public long image;
	public long buffer;
}
