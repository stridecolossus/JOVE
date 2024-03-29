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
	"viewFormatCount",
	"pViewFormats"
})
public class VkImageFormatListCreateInfoKHR extends VulkanStructure {
	public VkStructureType sType = VkStructureType.IMAGE_FORMAT_LIST_CREATE_INFO_KHR;
	public Pointer pNext;
	public int viewFormatCount;
	public Pointer pViewFormats;
}
