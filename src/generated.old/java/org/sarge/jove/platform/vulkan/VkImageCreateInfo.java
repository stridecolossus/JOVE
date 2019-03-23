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
	"flags",
	"imageType",
	"format",
	"extent",
	"mipLevels",
	"arrayLayers",
	"samples",
	"tiling",
	"usage",
	"sharingMode",
	"queueFamilyIndexCount",
	"pQueueFamilyIndices",
	"initialLayout"
})
public class VkImageCreateInfo extends Structure {
	public static class ByValue extends VkImageCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkImageCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public int imageType;
	public int format;
	public VkExtent3D extent;
	public int mipLevels;
	public int arrayLayers;
	public int samples;
	public int tiling;
	public int usage;
	public int sharingMode;
	public int queueFamilyIndexCount;
	public int pQueueFamilyIndices;
	public int initialLayout;
}
