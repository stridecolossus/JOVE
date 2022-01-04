package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"format",
	"colorSpace"
})
public class VkSurfaceFormatKHR extends VulkanStructure {
	public VkFormat format;
	public VkColorSpaceKHR colorSpace;
}
