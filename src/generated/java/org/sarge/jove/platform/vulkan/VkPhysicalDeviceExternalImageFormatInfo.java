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
	"handleType"
})
public class VkPhysicalDeviceExternalImageFormatInfo extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceExternalImageFormatInfo implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceExternalImageFormatInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_EXTERNAL_IMAGE_FORMAT_INFO;
	public Pointer pNext;
	public VkExternalMemoryHandleTypeFlagBits handleType;
}
