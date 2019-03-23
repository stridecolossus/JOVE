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
	"handleType",
	"pHostPointer"
})
public class VkImportMemoryHostPointerInfoEXT extends Structure {
	public static class ByValue extends VkImportMemoryHostPointerInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkImportMemoryHostPointerInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_IMPORT_MEMORY_HOST_POINTER_INFO_EXT.value();
	public Pointer pNext;
	public int handleType;
	public Pointer pHostPointer;
}
