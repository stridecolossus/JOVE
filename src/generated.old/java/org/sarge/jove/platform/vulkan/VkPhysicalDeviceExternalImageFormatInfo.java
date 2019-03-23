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
	"handleType"
})
public class VkPhysicalDeviceExternalImageFormatInfo extends Structure {
	public static class ByValue extends VkPhysicalDeviceExternalImageFormatInfo implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceExternalImageFormatInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_EXTERNAL_IMAGE_FORMAT_INFO.value();
	public Pointer pNext;
	public int handleType;
}
