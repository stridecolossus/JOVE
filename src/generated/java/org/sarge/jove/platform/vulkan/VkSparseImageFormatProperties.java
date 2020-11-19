package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"aspectMask",
	"imageGranularity",
	"flags"
})
public class VkSparseImageFormatProperties extends VulkanStructure {
	public static class ByValue extends VkSparseImageFormatProperties implements Structure.ByValue { }
	public static class ByReference extends VkSparseImageFormatProperties implements Structure.ByReference { }

	public VkImageAspectFlag aspectMask;
	public VkExtent3D imageGranularity;
	public int flags;
}
