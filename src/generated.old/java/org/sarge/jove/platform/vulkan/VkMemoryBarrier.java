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
	"srcAccessMask",
	"dstAccessMask"
})
public class VkMemoryBarrier extends Structure {
	public static class ByValue extends VkMemoryBarrier implements Structure.ByValue { }
	public static class ByReference extends VkMemoryBarrier implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_MEMORY_BARRIER.value();
	public Pointer pNext;
	public int srcAccessMask;
	public int dstAccessMask;
}
