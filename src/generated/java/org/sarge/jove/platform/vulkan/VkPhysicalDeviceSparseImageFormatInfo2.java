package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"format",
	"type",
	"samples",
	"usage",
	"tiling"
})
public class VkPhysicalDeviceSparseImageFormatInfo2 extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceSparseImageFormatInfo2 implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceSparseImageFormatInfo2 implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_SPARSE_IMAGE_FORMAT_INFO_2;
	public Pointer pNext;
	public VkFormat format;
	public VkImageType type;
	public VkSampleCount samples;
	public VkImageUsageFlag usage;
	public VkImageTiling tiling;
}
