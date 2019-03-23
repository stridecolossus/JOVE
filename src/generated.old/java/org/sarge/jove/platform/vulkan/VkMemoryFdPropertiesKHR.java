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
	"memoryTypeBits"
})
public class VkMemoryFdPropertiesKHR extends Structure {
	public static class ByValue extends VkMemoryFdPropertiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkMemoryFdPropertiesKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_MEMORY_FD_PROPERTIES_KHR.value();
	public Pointer pNext;
	public int memoryTypeBits;
}
