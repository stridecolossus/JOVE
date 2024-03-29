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
	"type",
	"flags",
	"instanceCount",
	"geometryCount",
	"pGeometries"
})
public class VkAccelerationStructureInfoNV extends VulkanStructure {
	public VkStructureType sType = VkStructureType.ACCELERATION_STRUCTURE_INFO_NV;
	public Pointer pNext;
	public VkAccelerationStructureTypeNV type;
	public int flags;
	public int instanceCount;
	public int geometryCount;
	public Pointer pGeometries;
}
