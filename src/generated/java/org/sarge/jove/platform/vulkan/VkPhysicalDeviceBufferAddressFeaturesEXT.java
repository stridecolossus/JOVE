package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

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
	"bufferDeviceAddress",
	"bufferDeviceAddressCaptureReplay",
	"bufferDeviceAddressMultiDevice"
})
public class VkPhysicalDeviceBufferAddressFeaturesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceBufferAddressFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceBufferAddressFeaturesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_BUFFER_ADDRESS_FEATURES_EXT;
	public Pointer pNext;
	public VulkanBoolean bufferDeviceAddress;
	public VulkanBoolean bufferDeviceAddressCaptureReplay;
	public VulkanBoolean bufferDeviceAddressMultiDevice;
}
