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
	"aabbData",
	"numAABBs",
	"stride",
	"offset"
})
public class VkGeometryAABBNV extends Structure {
	public static class ByValue extends VkGeometryAABBNV implements Structure.ByValue { }
	public static class ByReference extends VkGeometryAABBNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_GEOMETRY_AABBNV.value();
	public Pointer pNext;
	public long aabbData;
	public int numAABBs;
	public int stride;
	public long offset;
}
