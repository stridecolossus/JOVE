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
	"resourceDeviceIndex",
	"memoryDeviceIndex"
})
public class VkDeviceGroupBindSparseInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DEVICE_GROUP_BIND_SPARSE_INFO;
	public Pointer pNext;
	public int resourceDeviceIndex;
	public int memoryDeviceIndex;
}
