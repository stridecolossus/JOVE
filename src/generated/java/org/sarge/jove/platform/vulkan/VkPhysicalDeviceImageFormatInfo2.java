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
	"tiling",
	"usage",
	"flags"
})
public class VkPhysicalDeviceImageFormatInfo2 extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceImageFormatInfo2 implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceImageFormatInfo2 implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_IMAGE_FORMAT_INFO_2;
	public Pointer pNext;
	public VkFormat format;
	public VkImageType type;
	public VkImageTiling tiling;
	public VkImageUsageFlag usage;
	public int flags;
}
