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
	"flags",
	"image",
	"viewType",
	"format",
	"components",
	"subresourceRange"
})
public class VkImageViewCreateInfo extends VulkanStructure {
	public static class ByValue extends VkImageViewCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkImageViewCreateInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public Pointer image;
	public VkImageViewType viewType;
	public VkFormat format;
	public VkComponentMapping components;
	public VkImageSubresourceRange subresourceRange;
}