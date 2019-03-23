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
	"memory",
	"memoryOffset"
})
public class VkBindImageMemoryInfo extends Structure {
	public static class ByValue extends VkBindImageMemoryInfo implements Structure.ByValue { }
	public static class ByReference extends VkBindImageMemoryInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_BIND_IMAGE_MEMORY_INFO.value();
	public Pointer pNext;
	public long image;
	public long memory;
	public long memoryOffset;
}
