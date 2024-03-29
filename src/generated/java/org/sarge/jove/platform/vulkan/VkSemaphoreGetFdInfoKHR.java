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
	"semaphore",
	"handleType"
})
public class VkSemaphoreGetFdInfoKHR extends VulkanStructure {
	public static class ByValue extends VkSemaphoreGetFdInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkSemaphoreGetFdInfoKHR implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.SEMAPHORE_GET_FD_INFO_KHR;
	public Pointer pNext;
	public Pointer semaphore;
	public VkExternalSemaphoreHandleTypeFlag handleType;
}
