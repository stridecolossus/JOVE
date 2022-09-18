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
	"image"
})
public class VkImageMemoryRequirementsInfo2 extends VulkanStructure {
	public VkStructureType sType = VkStructureType.IMAGE_MEMORY_REQUIREMENTS_INFO_2;
	public Pointer pNext;
	public Pointer image;
}
