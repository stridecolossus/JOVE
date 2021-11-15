package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
public class VkExportMemoryAllocateInfo extends VulkanStructure {
	public static class ByValue extends VkExportMemoryAllocateInfo implements Structure.ByValue { }
	public static class ByReference extends VkExportMemoryAllocateInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.EXPORT_MEMORY_ALLOCATE_INFO;
	public Pointer pNext;
	public VkExternalMemoryHandleTypeFlag handleTypes;
}
