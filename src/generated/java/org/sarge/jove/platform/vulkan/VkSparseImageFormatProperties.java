package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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

	public VkImageAspect aspectMask;
	public VkExtent3D imageGranularity;
	public int flags;
}
