package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"queueFlags",
	"queueCount",
	"timestampValidBits",
	"minImageTransferGranularity"
})
public class VkQueueFamilyProperties extends VulkanStructure {
	public static class ByValue extends VkQueueFamilyProperties implements Structure.ByValue { }
	public static class ByReference extends VkQueueFamilyProperties implements Structure.ByReference { }

	public int queueFlags;
	public int queueCount;
	public int timestampValidBits;
	public VkExtent3D minImageTransferGranularity;
}
