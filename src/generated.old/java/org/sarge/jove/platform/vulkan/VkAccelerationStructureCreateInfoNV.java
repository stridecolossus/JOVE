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
	"compactedSize",
	"info"
})
public class VkAccelerationStructureCreateInfoNV extends Structure {
	public static class ByValue extends VkAccelerationStructureCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkAccelerationStructureCreateInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_CREATE_INFO_NV.value();
	public Pointer pNext;
	public long compactedSize;
	public VkAccelerationStructureInfoNV info;
}
