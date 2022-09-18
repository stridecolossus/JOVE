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
	"usage"
})
public class VkImageViewUsageCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.IMAGE_VIEW_USAGE_CREATE_INFO;
	public Pointer pNext;
	public VkImageUsageFlag usage;
}
