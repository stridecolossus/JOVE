package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"triangles",
	"aabbs"
})
public class VkGeometryDataNV extends VulkanStructure {
	public VkGeometryTrianglesNV triangles;
	public VkGeometryAABBNV aabbs;
}
