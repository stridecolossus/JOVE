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
	"samples",
	"usage",
	"tiling"
})
public class VkPhysicalDeviceSparseImageFormatInfo2 extends Structure {
	public static class ByValue extends VkPhysicalDeviceSparseImageFormatInfo2 implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceSparseImageFormatInfo2 implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SPARSE_IMAGE_FORMAT_INFO_2.value();
	public Pointer pNext;
	public int format;
	public int type;
	public int samples;
	public int usage;
	public int tiling;
}
