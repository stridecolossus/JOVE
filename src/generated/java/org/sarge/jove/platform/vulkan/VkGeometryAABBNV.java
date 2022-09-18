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
	"aabbData",
	"numAABBs",
	"stride",
	"offset"
})
public class VkGeometryAABBNV extends VulkanStructure {
	public VkStructureType sType = VkStructureType.GEOMETRY_AABB_NV;
	public Pointer pNext;
	public Pointer aabbData;
	public int numAABBs;
	public int stride;
	public long offset;
}
