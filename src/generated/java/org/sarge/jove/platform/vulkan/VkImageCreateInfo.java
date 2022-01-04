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
	"flags",
	"imageType",
	"format",
	"extent",
	"mipLevels",
	"arrayLayers",
	"samples",
	"tiling",
	"usage",
	"sharingMode",
	"queueFamilyIndexCount",
	"pQueueFamilyIndices",
	"initialLayout"
})
public class VkImageCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.IMAGE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public VkImageType imageType;
	public VkFormat format;
	public VkExtent3D extent;
	public int mipLevels;
	public int arrayLayers;
	public VkSampleCount samples;
	public VkImageTiling tiling;
	public int usage;
	public VkSharingMode sharingMode;
	public int queueFamilyIndexCount;
	public Pointer pQueueFamilyIndices;
	public VkImageLayout initialLayout;
}
