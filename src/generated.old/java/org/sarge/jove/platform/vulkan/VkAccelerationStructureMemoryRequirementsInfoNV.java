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
	"type",
	"accelerationStructure"
})
public class VkAccelerationStructureMemoryRequirementsInfoNV extends Structure {
	public static class ByValue extends VkAccelerationStructureMemoryRequirementsInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkAccelerationStructureMemoryRequirementsInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_MEMORY_REQUIREMENTS_INFO_NV.value();
	public Pointer pNext;
	public int type;
	public long accelerationStructure;
}
