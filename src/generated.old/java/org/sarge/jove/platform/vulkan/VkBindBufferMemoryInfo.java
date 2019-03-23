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
	"buffer",
	"memory",
	"memoryOffset"
})
public class VkBindBufferMemoryInfo extends Structure {
	public static class ByValue extends VkBindBufferMemoryInfo implements Structure.ByValue { }
	public static class ByReference extends VkBindBufferMemoryInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_BIND_BUFFER_MEMORY_INFO.value();
	public Pointer pNext;
	public long buffer;
	public long memory;
	public long memoryOffset;
}
