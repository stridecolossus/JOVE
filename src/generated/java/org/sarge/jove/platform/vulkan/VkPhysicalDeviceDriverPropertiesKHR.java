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
	"driverID",
	"driverName",
	"driverInfo",
	"conformanceVersion"
})
public class VkPhysicalDeviceDriverPropertiesKHR extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceDriverPropertiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceDriverPropertiesKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_DRIVER_PROPERTIES_KHR;
	public Pointer pNext;
	public VkDriverIdKHR driverID;
	public byte[] driverName = new byte[256];
	public byte[] driverInfo = new byte[256];
	public VkConformanceVersionKHR conformanceVersion;
}
