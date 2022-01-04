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
	"stencilUsage"
})
public class VkImageStencilUsageCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkImageStencilUsageCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkImageStencilUsageCreateInfoEXT implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.IMAGE_STENCIL_USAGE_CREATE_INFO_EXT;
	public Pointer pNext;
	public VkImageUsageFlag stencilUsage;
}
