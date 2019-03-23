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
	"allocationSize",
	"memoryTypeIndex"
})
public class VkMemoryAllocateInfo extends Structure {
	public static class ByValue extends VkMemoryAllocateInfo implements Structure.ByValue { }
	public static class ByReference extends VkMemoryAllocateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO.value();
	public Pointer pNext;
	public long allocationSize;
	public int memoryTypeIndex;
}
