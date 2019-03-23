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
	"bufferDeviceAddress",
	"bufferDeviceAddressCaptureReplay",
	"bufferDeviceAddressMultiDevice"
})
public class VkPhysicalDeviceBufferAddressFeaturesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceBufferAddressFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceBufferAddressFeaturesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_BUFFER_ADDRESS_FEATURES_EXT.value();
	public Pointer pNext;
	public boolean bufferDeviceAddress;
	public boolean bufferDeviceAddressCaptureReplay;
	public boolean bufferDeviceAddressMultiDevice;
}
