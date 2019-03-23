package org.sarge.jove.platform.vulkan;

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
	"flags",
	"image",
	"viewType",
	"format",
	"components",
	"subresourceRange"
})
public class VkImageViewCreateInfo extends Structure {
	public static class ByValue extends VkImageViewCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkImageViewCreateInfo implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public Pointer image;
	public int viewType;
	public int format;
	public VkComponentMapping components;
	public VkImageSubresourceRange subresourceRange;
}
