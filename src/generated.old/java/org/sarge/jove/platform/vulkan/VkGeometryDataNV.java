package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"triangles",
	"aabbs"
})
public class VkGeometryDataNV extends Structure {
	public static class ByValue extends VkGeometryDataNV implements Structure.ByValue { }
	public static class ByReference extends VkGeometryDataNV implements Structure.ByReference { }
	
	public VkGeometryTrianglesNV triangles;
	public VkGeometryAABBNV aabbs;
}
