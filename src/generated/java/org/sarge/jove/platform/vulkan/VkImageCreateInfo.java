package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkImageCreateInfo extends VulkanStructure {
	public final VkStructureType sType = VkStructureType.IMAGE_CREATE_INFO;
	public Handle pNext;
	public EnumMask<VkImageCreateFlag> flags;
	public VkImageType imageType;
	public VkFormat format;
	public VkExtent3D extent;
	public int mipLevels;
	public int arrayLayers;
	public VkSampleCount samples;
	public VkImageTiling tiling;
	public EnumMask<VkImageUsageFlag> usage;
	public VkSharingMode sharingMode;
	public int queueFamilyIndexCount;
	public int[] pQueueFamilyIndices;
	public VkImageLayout initialLayout;
}
