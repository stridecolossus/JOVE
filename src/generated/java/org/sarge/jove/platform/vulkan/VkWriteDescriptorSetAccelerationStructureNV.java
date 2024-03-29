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
	"accelerationStructureCount",
	"pAccelerationStructures"
})
public class VkWriteDescriptorSetAccelerationStructureNV extends VulkanStructure {
	public VkStructureType sType = VkStructureType.WRITE_DESCRIPTOR_SET_ACCELERATION_STRUCTURE_NV;
	public Pointer pNext;
	public int accelerationStructureCount;
	public Pointer pAccelerationStructures;
}
