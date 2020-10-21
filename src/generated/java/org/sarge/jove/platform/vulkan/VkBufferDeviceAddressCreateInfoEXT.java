package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"deviceAddress"
})
public class VkBufferDeviceAddressCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkBufferDeviceAddressCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkBufferDeviceAddressCreateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_BUFFER_DEVICE_ADDRESS_CREATE_INFO_EXT;
	public Pointer pNext;
	public long deviceAddress;
}
