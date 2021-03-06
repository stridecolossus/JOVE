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
	"handleTypes"
})
public class VkExportSemaphoreCreateInfo extends VulkanStructure {
	public static class ByValue extends VkExportSemaphoreCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkExportSemaphoreCreateInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_EXPORT_SEMAPHORE_CREATE_INFO;
	public Pointer pNext;
	public VkExternalSemaphoreHandleTypeFlag handleTypes;
}
