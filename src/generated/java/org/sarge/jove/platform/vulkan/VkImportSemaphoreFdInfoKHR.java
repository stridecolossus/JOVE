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
	"semaphore",
	"flags",
	"handleType",
	"fd"
})
public class VkImportSemaphoreFdInfoKHR extends VulkanStructure {
	public VkStructureType sType = VkStructureType.IMPORT_SEMAPHORE_FD_INFO_KHR;
	public Pointer pNext;
	public Pointer semaphore;
	public int flags;
	public VkExternalSemaphoreHandleTypeFlag handleType;
	public int fd;
}
