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
	"viewFormatCount",
	"pViewFormats"
})
public class VkImageFormatListCreateInfoKHR extends VulkanStructure {
	public static class ByValue extends VkImageFormatListCreateInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkImageFormatListCreateInfoKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_IMAGE_FORMAT_LIST_CREATE_INFO_KHR;
	public Pointer pNext;
	public int viewFormatCount;
	public Pointer pViewFormats;
}
