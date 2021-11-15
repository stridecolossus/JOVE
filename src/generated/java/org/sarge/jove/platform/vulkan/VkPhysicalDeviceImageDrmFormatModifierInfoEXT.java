package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
	"drmFormatModifier",
	"sharingMode",
	"queueFamilyIndexCount",
	"pQueueFamilyIndices"
})
public class VkPhysicalDeviceImageDrmFormatModifierInfoEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceImageDrmFormatModifierInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceImageDrmFormatModifierInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_IMAGE_DRM_FORMAT_MODIFIER_INFO_EXT;
	public Pointer pNext;
	public long drmFormatModifier;
	public VkSharingMode sharingMode;
	public int queueFamilyIndexCount;
	public Pointer pQueueFamilyIndices;
}
