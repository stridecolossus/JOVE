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
	"scalarBlockLayout"
})
public class VkPhysicalDeviceScalarBlockLayoutFeaturesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceScalarBlockLayoutFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceScalarBlockLayoutFeaturesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SCALAR_BLOCK_LAYOUT_FEATURES_EXT.value();
	public Pointer pNext;
	public boolean scalarBlockLayout;
}
