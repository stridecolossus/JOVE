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
public class VkExportMemoryAllocateInfoNV extends Structure {
	public static class ByValue extends VkExportMemoryAllocateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkExportMemoryAllocateInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_EXPORT_MEMORY_ALLOCATE_INFO_NV.value();
	public Pointer pNext;
	public int handleTypes;
}
