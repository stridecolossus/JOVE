package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

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
public class VkImportMemoryHostPointerInfoEXT extends VulkanStructure {
	public VkStructureType sType = VkStructureType.IMPORT_MEMORY_HOST_POINTER_INFO_EXT;
	public Pointer pNext;
	public VkExternalMemoryHandleTypeFlag handleType;
	public Pointer pHostPointer;
}
