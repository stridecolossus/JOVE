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
	"driverID",
	"driverName",
	"driverInfo",
	"conformanceVersion"
})
public class VkPhysicalDeviceDriverPropertiesKHR extends Structure {
	public static class ByValue extends VkPhysicalDeviceDriverPropertiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceDriverPropertiesKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_DRIVER_PROPERTIES_KHR.value();
	public Pointer pNext;
	public int driverID;
	public final byte[] driverName = new byte[256];
	public final byte[] driverInfo = new byte[256];
	public VkConformanceVersionKHR conformanceVersion;
}
