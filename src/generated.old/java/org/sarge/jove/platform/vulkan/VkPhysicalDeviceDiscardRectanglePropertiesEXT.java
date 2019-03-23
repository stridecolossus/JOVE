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
	"maxDiscardRectangles"
})
public class VkPhysicalDeviceDiscardRectanglePropertiesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceDiscardRectanglePropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceDiscardRectanglePropertiesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_DISCARD_RECTANGLE_PROPERTIES_EXT.value();
	public Pointer pNext;
	public int maxDiscardRectangles;
}
