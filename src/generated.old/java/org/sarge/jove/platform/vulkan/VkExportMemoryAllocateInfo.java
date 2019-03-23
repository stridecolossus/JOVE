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
	"handleTypes"
})
public class VkExportMemoryAllocateInfo extends Structure {
	public static class ByValue extends VkExportMemoryAllocateInfo implements Structure.ByValue { }
	public static class ByReference extends VkExportMemoryAllocateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_EXPORT_MEMORY_ALLOCATE_INFO.value();
	public Pointer pNext;
	public int handleTypes;
}
