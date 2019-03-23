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
	"viewFormatCount",
	"pViewFormats"
})
public class VkImageFormatListCreateInfoKHR extends Structure {
	public static class ByValue extends VkImageFormatListCreateInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkImageFormatListCreateInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_IMAGE_FORMAT_LIST_CREATE_INFO_KHR.value();
	public Pointer pNext;
	public int viewFormatCount;
	public int pViewFormats;
}
