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
public class VkExportFenceCreateInfo extends VulkanStructure {
	public static class ByValue extends VkExportFenceCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkExportFenceCreateInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.EXPORT_FENCE_CREATE_INFO;
	public Pointer pNext;
	public VkExternalFenceHandleTypeFlag handleTypes;
}
