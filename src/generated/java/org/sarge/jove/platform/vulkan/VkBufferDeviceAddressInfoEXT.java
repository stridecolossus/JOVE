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
	"buffer"
})
public class VkBufferDeviceAddressInfoEXT extends VulkanStructure {
	public static class ByValue extends VkBufferDeviceAddressInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkBufferDeviceAddressInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_BUFFER_DEVICE_ADDRESS_INFO_EXT;
	public Pointer pNext;
	public Pointer buffer;
}
