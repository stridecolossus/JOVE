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
public class VkExternalMemoryImageCreateInfoNV extends VulkanStructure {
	public VkStructureType sType = VkStructureType.EXTERNAL_MEMORY_IMAGE_CREATE_INFO_NV;
	public Pointer pNext;
	public VkExternalMemoryHandleTypeFlagNV handleTypes;
}
