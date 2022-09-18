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
	"overallocationBehavior"
})
public class VkDeviceMemoryOverallocationCreateInfoAMD extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DEVICE_MEMORY_OVERALLOCATION_CREATE_INFO_AMD;
	public Pointer pNext;
	public VkMemoryOverallocationBehaviorAMD overallocationBehavior;
}
