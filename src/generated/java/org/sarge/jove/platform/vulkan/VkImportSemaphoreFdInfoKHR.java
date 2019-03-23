package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
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
	"semaphore",
	"flags",
	"handleType",
	"fd"
})
public class VkImportSemaphoreFdInfoKHR extends VulkanStructure {
	public static class ByValue extends VkImportSemaphoreFdInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkImportSemaphoreFdInfoKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_IMPORT_SEMAPHORE_FD_INFO_KHR;
	public Pointer pNext;
	public Pointer semaphore;
	public int flags;
	public VkExternalSemaphoreHandleTypeFlagBits handleType;
	public int fd;
}
