package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"maxExtent",
	"maxMipLevels",
	"maxArrayLayers",
	"sampleCounts",
	"maxResourceSize"
})
public class VkImageFormatProperties extends VulkanStructure {
	public VkExtent3D maxExtent;
	public int maxMipLevels;
	public int maxArrayLayers;
	public VkSampleCount sampleCounts;
	public long maxResourceSize;
}
