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
	"type",
	"flags",
	"instanceCount",
	"geometryCount",
	"pGeometries"
})
public class VkAccelerationStructureInfoNV extends Structure {
	public static class ByValue extends VkAccelerationStructureInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkAccelerationStructureInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_INFO_NV.value();
	public Pointer pNext;
	public int type;
	public int flags;
	public int instanceCount;
	public int geometryCount;
	public VkGeometryNV.ByReference pGeometries;
}
