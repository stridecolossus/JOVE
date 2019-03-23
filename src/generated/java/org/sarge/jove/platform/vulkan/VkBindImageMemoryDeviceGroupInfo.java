package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
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
	"deviceIndexCount",
	"pDeviceIndices",
	"splitInstanceBindRegionCount",
	"pSplitInstanceBindRegions"
})
public class VkBindImageMemoryDeviceGroupInfo extends VulkanStructure {
	public static class ByValue extends VkBindImageMemoryDeviceGroupInfo implements Structure.ByValue { }
	public static class ByReference extends VkBindImageMemoryDeviceGroupInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_BIND_IMAGE_MEMORY_DEVICE_GROUP_INFO;
	public Pointer pNext;
	public int deviceIndexCount;
	public Pointer pDeviceIndices;
	public int splitInstanceBindRegionCount;
	public Pointer pSplitInstanceBindRegions;
}
