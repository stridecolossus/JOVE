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
	"usage"
})
public class VkImageViewUsageCreateInfo extends VulkanStructure {
	public static class ByValue extends VkImageViewUsageCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkImageViewUsageCreateInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_IMAGE_VIEW_USAGE_CREATE_INFO;
	public Pointer pNext;
	public VkImageUsageFlags usage;
}
