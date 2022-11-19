package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

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
	public BitMask<VkQueueFlag> queueFlags;
	public int queueCount;
	public int timestampValidBits;
	public VkExtent3D minImageTransferGranularity;
}
