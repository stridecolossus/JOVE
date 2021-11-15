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
	"handleType",
	"fd"
})
public class VkImportMemoryFdInfoKHR extends VulkanStructure {
	public static class ByValue extends VkImportMemoryFdInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkImportMemoryFdInfoKHR implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.IMPORT_MEMORY_FD_INFO_KHR;
	public Pointer pNext;
	public VkExternalMemoryHandleTypeFlag handleType;
	public int fd;
}
