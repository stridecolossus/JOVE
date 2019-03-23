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
	"pointClippingBehavior"
})
public class VkPhysicalDevicePointClippingProperties extends Structure {
	public static class ByValue extends VkPhysicalDevicePointClippingProperties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDevicePointClippingProperties implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_POINT_CLIPPING_PROPERTIES.value();
	public Pointer pNext;
	public int pointClippingBehavior;
}
