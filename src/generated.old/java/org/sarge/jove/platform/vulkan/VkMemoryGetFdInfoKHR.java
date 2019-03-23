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
	"handleType"
})
public class VkMemoryGetFdInfoKHR extends Structure {
	public static class ByValue extends VkMemoryGetFdInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkMemoryGetFdInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_MEMORY_GET_FD_INFO_KHR.value();
	public Pointer pNext;
	public long memory;
	public int handleType;
}
