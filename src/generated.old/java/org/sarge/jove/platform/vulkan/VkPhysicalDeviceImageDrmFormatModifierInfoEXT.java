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
	"drmFormatModifier",
	"sharingMode",
	"queueFamilyIndexCount",
	"pQueueFamilyIndices"
})
public class VkPhysicalDeviceImageDrmFormatModifierInfoEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceImageDrmFormatModifierInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceImageDrmFormatModifierInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_IMAGE_DRM_FORMAT_MODIFIER_INFO_EXT.value();
	public Pointer pNext;
	public long drmFormatModifier;
	public int sharingMode;
	public int queueFamilyIndexCount;
	public int pQueueFamilyIndices;
}
