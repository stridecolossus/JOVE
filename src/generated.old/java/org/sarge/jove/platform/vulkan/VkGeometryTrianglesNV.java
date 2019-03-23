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
	"vertexData",
	"vertexOffset",
	"vertexCount",
	"vertexStride",
	"vertexFormat",
	"indexData",
	"indexOffset",
	"indexCount",
	"indexType",
	"transformData",
	"transformOffset"
})
public class VkGeometryTrianglesNV extends Structure {
	public static class ByValue extends VkGeometryTrianglesNV implements Structure.ByValue { }
	public static class ByReference extends VkGeometryTrianglesNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_GEOMETRY_TRIANGLES_NV.value();
	public Pointer pNext;
	public long vertexData;
	public long vertexOffset;
	public int vertexCount;
	public long vertexStride;
	public int vertexFormat;
	public long indexData;
	public long indexOffset;
	public int indexCount;
	public int indexType;
	public long transformData;
	public long transformOffset;
}
