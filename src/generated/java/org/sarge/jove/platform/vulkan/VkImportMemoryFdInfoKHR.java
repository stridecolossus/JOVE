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
	"fd"
})
public class VkImportMemoryFdInfoKHR extends VulkanStructure {
	public VkStructureType sType = VkStructureType.IMPORT_MEMORY_FD_INFO_KHR;
	public Pointer pNext;
	public VkExternalMemoryHandleTypeFlag handleType;
	public int fd;
}
