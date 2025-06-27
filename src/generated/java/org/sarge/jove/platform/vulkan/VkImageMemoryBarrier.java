package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkImageMemoryBarrier extends VulkanStructure {
	public VkStructureType sType = VkStructureType.IMAGE_MEMORY_BARRIER;
	public Handle pNext;
	public EnumMask<VkAccess> srcAccessMask;
	public EnumMask<VkAccess> dstAccessMask;
	public VkImageLayout oldLayout;
	public VkImageLayout newLayout;
	public int srcQueueFamilyIndex;
	public int dstQueueFamilyIndex;
	public Handle image;
	public VkImageSubresourceRange subresourceRange;
}
