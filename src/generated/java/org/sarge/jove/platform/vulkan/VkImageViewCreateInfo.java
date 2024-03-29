package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Pointer;
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
	public VkStructureType sType = VkStructureType.IMAGE_VIEW_CREATE_INFO;
	public Pointer pNext;
	public BitMask<VkImageViewCreateFlag> flags;
	public Handle image;
	public VkImageViewType viewType;
	public VkFormat format;
	public VkComponentMapping components;
	public VkImageSubresourceRange subresourceRange;
}
