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
	"geometryType",
	"geometry",
	"flags"
})
public class VkGeometryNV extends Structure {
	public static class ByValue extends VkGeometryNV implements Structure.ByValue { }
	public static class ByReference extends VkGeometryNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_GEOMETRY_NV.value();
	public Pointer pNext;
	public int geometryType;
	public VkGeometryDataNV geometry;
	public int flags;
}
