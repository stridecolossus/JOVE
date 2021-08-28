package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"imageViewType"
})
public class VkPhysicalDeviceImageViewImageFormatInfoEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceImageViewImageFormatInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceImageViewImageFormatInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_IMAGE_VIEW_IMAGE_FORMAT_INFO_EXT;
	public Pointer pNext;
	public VkImageViewType imageViewType;
}
