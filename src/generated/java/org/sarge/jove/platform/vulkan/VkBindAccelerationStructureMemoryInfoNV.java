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
	"accelerationStructure",
	"memory",
	"memoryOffset",
	"deviceIndexCount",
	"pDeviceIndices"
})
public class VkBindAccelerationStructureMemoryInfoNV extends VulkanStructure {
	public VkStructureType sType = VkStructureType.BIND_ACCELERATION_STRUCTURE_MEMORY_INFO_NV;
	public Pointer pNext;
	public long accelerationStructure;
	public Pointer memory;
	public long memoryOffset;
	public int deviceIndexCount;
	public Pointer pDeviceIndices;
}
