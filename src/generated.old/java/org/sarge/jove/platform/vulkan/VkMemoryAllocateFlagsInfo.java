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
	"flags",
	"deviceMask"
})
public class VkMemoryAllocateFlagsInfo extends Structure {
	public static class ByValue extends VkMemoryAllocateFlagsInfo implements Structure.ByValue { }
	public static class ByReference extends VkMemoryAllocateFlagsInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_FLAGS_INFO.value();
	public Pointer pNext;
	public int flags;
	public int deviceMask;
}
