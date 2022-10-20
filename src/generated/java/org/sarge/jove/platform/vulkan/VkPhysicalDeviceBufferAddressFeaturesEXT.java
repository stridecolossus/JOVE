package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.*;
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

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_BUFFER_ADDRESS_FEATURES_EXT;
	public Pointer pNext;
	public boolean bufferDeviceAddress;
	public boolean bufferDeviceAddressCaptureReplay;
	public boolean bufferDeviceAddressMultiDevice;
}
