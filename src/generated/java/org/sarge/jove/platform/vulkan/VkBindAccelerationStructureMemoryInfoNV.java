package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"accelerationStructure",
	"memory",
	"memoryOffset",
	"deviceIndexCount",
	"pDeviceIndices"
})
public class VkBindAccelerationStructureMemoryInfoNV extends VulkanStructure {
	public static class ByValue extends VkBindAccelerationStructureMemoryInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkBindAccelerationStructureMemoryInfoNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.BIND_ACCELERATION_STRUCTURE_MEMORY_INFO_NV;
	public Pointer pNext;
	public long accelerationStructure;
	public Pointer memory;
	public long memoryOffset;
	public int deviceIndexCount;
	public Pointer pDeviceIndices;
}
