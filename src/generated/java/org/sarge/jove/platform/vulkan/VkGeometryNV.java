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
	"geometryType",
	"geometry",
	"flags"
})
public class VkGeometryNV extends VulkanStructure {
	public VkStructureType sType = VkStructureType.GEOMETRY_NV;
	public Pointer pNext;
	public VkGeometryTypeNV geometryType;
	public VkGeometryDataNV geometry;
	public int flags;
}
