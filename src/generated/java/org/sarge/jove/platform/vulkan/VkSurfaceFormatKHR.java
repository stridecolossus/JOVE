package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
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
	public static class ByValue extends VkSurfaceFormatKHR implements Structure.ByValue { }
	public static class ByReference extends VkSurfaceFormatKHR implements Structure.ByReference { }
	
	public VkFormat format;
	public VkColorSpaceKHR colorSpace;
}
