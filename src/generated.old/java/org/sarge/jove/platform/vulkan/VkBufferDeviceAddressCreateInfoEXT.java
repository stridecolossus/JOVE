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
	"deviceAddress"
})
public class VkBufferDeviceAddressCreateInfoEXT extends Structure {
	public static class ByValue extends VkBufferDeviceAddressCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkBufferDeviceAddressCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_BUFFER_DEVICE_ADDRESS_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public long deviceAddress;
}
