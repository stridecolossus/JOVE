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
	"buffer"
})
public class VkBufferDeviceAddressInfoEXT extends Structure {
	public static class ByValue extends VkBufferDeviceAddressInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkBufferDeviceAddressInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_BUFFER_DEVICE_ADDRESS_INFO_EXT.value();
	public Pointer pNext;
	public long buffer;
}
