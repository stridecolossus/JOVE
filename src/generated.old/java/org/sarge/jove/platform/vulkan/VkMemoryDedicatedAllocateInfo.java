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
public class VkMemoryDedicatedAllocateInfo extends Structure {
	public static class ByValue extends VkMemoryDedicatedAllocateInfo implements Structure.ByValue { }
	public static class ByReference extends VkMemoryDedicatedAllocateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_MEMORY_DEDICATED_ALLOCATE_INFO.value();
	public Pointer pNext;
	public long image;
	public long buffer;
}
