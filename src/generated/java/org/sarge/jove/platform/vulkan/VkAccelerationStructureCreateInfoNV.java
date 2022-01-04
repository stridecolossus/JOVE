package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"compactedSize",
	"info"
})
public class VkAccelerationStructureCreateInfoNV extends VulkanStructure {
	public static class ByValue extends VkAccelerationStructureCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkAccelerationStructureCreateInfoNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.ACCELERATION_STRUCTURE_CREATE_INFO_NV;
	public Pointer pNext;
	public long compactedSize;
	public VkAccelerationStructureInfoNV info;
}
