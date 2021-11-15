package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
	"type",
	"accelerationStructure"
})
public class VkAccelerationStructureMemoryRequirementsInfoNV extends VulkanStructure {
	public static class ByValue extends VkAccelerationStructureMemoryRequirementsInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkAccelerationStructureMemoryRequirementsInfoNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.ACCELERATION_STRUCTURE_MEMORY_REQUIREMENTS_INFO_NV;
	public Pointer pNext;
	public VkAccelerationStructureMemoryRequirementsTypeNV type;
	public long accelerationStructure;
}
