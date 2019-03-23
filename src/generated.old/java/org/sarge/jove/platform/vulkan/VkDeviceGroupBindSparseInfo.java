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
	"resourceDeviceIndex",
	"memoryDeviceIndex"
})
public class VkDeviceGroupBindSparseInfo extends Structure {
	public static class ByValue extends VkDeviceGroupBindSparseInfo implements Structure.ByValue { }
	public static class ByReference extends VkDeviceGroupBindSparseInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_GROUP_BIND_SPARSE_INFO.value();
	public Pointer pNext;
	public int resourceDeviceIndex;
	public int memoryDeviceIndex;
}
