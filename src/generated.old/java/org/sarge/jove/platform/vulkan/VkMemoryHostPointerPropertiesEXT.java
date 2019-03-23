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
public class VkMemoryHostPointerPropertiesEXT extends Structure {
	public static class ByValue extends VkMemoryHostPointerPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkMemoryHostPointerPropertiesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_MEMORY_HOST_POINTER_PROPERTIES_EXT.value();
	public Pointer pNext;
	public int memoryTypeBits;
}
