package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure;
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
	public static class ByValue extends VkImageFormatProperties implements Structure.ByValue { }
	public static class ByReference extends VkImageFormatProperties implements Structure.ByReference { }

	public VkExtent3D maxExtent;
	public int maxMipLevels;
	public int maxArrayLayers;
	public VkSampleCount sampleCounts;
	public long maxResourceSize;
}
