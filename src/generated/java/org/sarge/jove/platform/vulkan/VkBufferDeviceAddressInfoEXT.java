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
	"buffer"
})
public class VkBufferDeviceAddressInfoEXT extends VulkanStructure {
	public VkStructureType sType = VkStructureType.BUFFER_DEVICE_ADDRESS_INFO_EXT;
	public Pointer pNext;
	public Pointer buffer;
}
