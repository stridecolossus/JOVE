package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

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
public class VkWriteDescriptorSetAccelerationStructureNV extends Structure {
	public static class ByValue extends VkWriteDescriptorSetAccelerationStructureNV implements Structure.ByValue { }
	public static class ByReference extends VkWriteDescriptorSetAccelerationStructureNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET_ACCELERATION_STRUCTURE_NV.value();
	public Pointer pNext;
	public int accelerationStructureCount;
	public long pAccelerationStructures;
}
