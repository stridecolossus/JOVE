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
	"imageViewType"
})
public class VkPhysicalDeviceImageViewImageFormatInfoEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceImageViewImageFormatInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceImageViewImageFormatInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_IMAGE_VIEW_IMAGE_FORMAT_INFO_EXT.value();
	public Pointer pNext;
	public int imageViewType;
}
