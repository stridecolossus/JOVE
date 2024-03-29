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
public class VkGeometryTrianglesNV extends VulkanStructure {
	public VkStructureType sType = VkStructureType.GEOMETRY_TRIANGLES_NV;
	public Pointer pNext;
	public Pointer vertexData;
	public long vertexOffset;
	public int vertexCount;
	public long vertexStride;
	public VkFormat vertexFormat;
	public Pointer indexData;
	public long indexOffset;
	public int indexCount;
	public VkIndexType indexType;
	public Pointer transformData;
	public long transformOffset;
}
