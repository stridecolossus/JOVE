package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkImageViewCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.IMAGE_VIEW_CREATE_INFO;
	public Handle pNext;
	public BitMask<VkImageViewCreateFlag> flags;
	public Handle image;
	public VkImageViewType viewType;
	public VkFormat format;
	public VkComponentMapping components;
	public VkImageSubresourceRange subresourceRange;
}
