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
	"fd"
})
public class VkImportMemoryFdInfoKHR extends Structure {
	public static class ByValue extends VkImportMemoryFdInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkImportMemoryFdInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_IMPORT_MEMORY_FD_INFO_KHR.value();
	public Pointer pNext;
	public int handleType;
	public int fd;
}
