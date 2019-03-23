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
	"format",
	"type",
	"tiling",
	"usage",
	"flags"
})
public class VkPhysicalDeviceImageFormatInfo2 extends Structure {
	public static class ByValue extends VkPhysicalDeviceImageFormatInfo2 implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceImageFormatInfo2 implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_IMAGE_FORMAT_INFO_2.value();
	public Pointer pNext;
	public int format;
	public int type;
	public int tiling;
	public int usage;
	public int flags;
}
