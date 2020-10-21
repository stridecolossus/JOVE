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
	"type",
	"flags",
	"instanceCount",
	"geometryCount",
	"pGeometries"
})
public class VkAccelerationStructureInfoNV extends VulkanStructure {
	public static class ByValue extends VkAccelerationStructureInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkAccelerationStructureInfoNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_INFO_NV;
	public Pointer pNext;
	public VkAccelerationStructureTypeNV type;
	public int flags;
	public int instanceCount;
	public int geometryCount;
	public Pointer pGeometries;
}
