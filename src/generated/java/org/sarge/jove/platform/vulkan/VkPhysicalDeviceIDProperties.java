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
	"deviceUUID",
	"driverUUID",
	"deviceLUID",
	"deviceNodeMask",
	"deviceLUIDValid"
})
public class VkPhysicalDeviceIDProperties extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceIDProperties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceIDProperties implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_ID_PROPERTIES;
	public Pointer pNext;
	public byte[] deviceUUID = new byte[16];
	public byte[] driverUUID = new byte[16];
	public byte[] deviceLUID = new byte[8];
	public int deviceNodeMask;
	public boolean deviceLUIDValid;
}
