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
	"memory",
	"offset",
	"size"
})
public class VkMappedMemoryRange extends Structure {
	public static class ByValue extends VkMappedMemoryRange implements Structure.ByValue { }
	public static class ByReference extends VkMappedMemoryRange implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE.value();
	public Pointer pNext;
	public long memory;
	public long offset;
	public long size;
}
