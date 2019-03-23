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
	"accelerationStructure",
	"memory",
	"memoryOffset",
	"deviceIndexCount",
	"pDeviceIndices"
})
public class VkBindAccelerationStructureMemoryInfoNV extends Structure {
	public static class ByValue extends VkBindAccelerationStructureMemoryInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkBindAccelerationStructureMemoryInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_BIND_ACCELERATION_STRUCTURE_MEMORY_INFO_NV.value();
	public Pointer pNext;
	public long accelerationStructure;
	public long memory;
	public long memoryOffset;
	public int deviceIndexCount;
	public int pDeviceIndices;
}
