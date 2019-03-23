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
	"deviceUUID",
	"driverUUID",
	"deviceLUID",
	"deviceNodeMask",
	"deviceLUIDValid"
})
public class VkPhysicalDeviceIDProperties extends Structure {
	public static class ByValue extends VkPhysicalDeviceIDProperties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceIDProperties implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_ID_PROPERTIES.value();
	public Pointer pNext;
	public final byte[] deviceUUID = new byte[16];
	public final byte[] driverUUID = new byte[16];
	public final byte[] deviceLUID = new byte[8];
	public int deviceNodeMask;
	public boolean deviceLUIDValid;
}
