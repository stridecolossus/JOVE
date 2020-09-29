package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"handleType",
	"pHostPointer"
})
public class VkImportMemoryHostPointerInfoEXT extends VulkanStructure {
	public static class ByValue extends VkImportMemoryHostPointerInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkImportMemoryHostPointerInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_IMPORT_MEMORY_HOST_POINTER_INFO_EXT;
	public Pointer pNext;
	public VkExternalMemoryHandleTypeFlagBits handleType;
	public Pointer pHostPointer;
}
