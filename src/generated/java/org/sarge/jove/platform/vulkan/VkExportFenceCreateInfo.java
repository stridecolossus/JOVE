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
	"handleTypes"
})
public class VkExportFenceCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.EXPORT_FENCE_CREATE_INFO;
	public Pointer pNext;
	public VkExternalFenceHandleTypeFlag handleTypes;
}
