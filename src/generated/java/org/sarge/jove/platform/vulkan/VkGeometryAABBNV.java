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
	"aabbData",
	"numAABBs",
	"stride",
	"offset"
})
public class VkGeometryAABBNV extends VulkanStructure {
	public static class ByValue extends VkGeometryAABBNV implements Structure.ByValue { }
	public static class ByReference extends VkGeometryAABBNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.GEOMETRY_AABB_NV;
	public Pointer pNext;
	public Pointer aabbData;
	public int numAABBs;
	public int stride;
	public long offset;
}
