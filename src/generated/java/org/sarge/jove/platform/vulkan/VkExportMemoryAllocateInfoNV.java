package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"handleTypes"
})
public class VkExportMemoryAllocateInfoNV extends VulkanStructure {
	public static class ByValue extends VkExportMemoryAllocateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkExportMemoryAllocateInfoNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.EXPORT_MEMORY_ALLOCATE_INFO_NV;
	public Pointer pNext;
	public VkExternalMemoryHandleTypeFlagNV handleTypes;
}
