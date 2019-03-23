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
	"usage"
})
public class VkImageViewUsageCreateInfo extends Structure {
	public static class ByValue extends VkImageViewUsageCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkImageViewUsageCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_IMAGE_VIEW_USAGE_CREATE_INFO.value();
	public Pointer pNext;
	public int usage;
}
