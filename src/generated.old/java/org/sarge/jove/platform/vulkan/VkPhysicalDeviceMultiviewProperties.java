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
	"maxMultiviewViewCount",
	"maxMultiviewInstanceIndex"
})
public class VkPhysicalDeviceMultiviewProperties extends Structure {
	public static class ByValue extends VkPhysicalDeviceMultiviewProperties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMultiviewProperties implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MULTIVIEW_PROPERTIES.value();
	public Pointer pNext;
	public int maxMultiviewViewCount;
	public int maxMultiviewInstanceIndex;
}
