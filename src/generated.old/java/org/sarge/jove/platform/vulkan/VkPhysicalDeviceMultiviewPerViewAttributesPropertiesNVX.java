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
	"perViewPositionAllComponents"
})
public class VkPhysicalDeviceMultiviewPerViewAttributesPropertiesNVX extends Structure {
	public static class ByValue extends VkPhysicalDeviceMultiviewPerViewAttributesPropertiesNVX implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMultiviewPerViewAttributesPropertiesNVX implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MULTIVIEW_PER_VIEW_ATTRIBUTES_PROPERTIES_NVX.value();
	public Pointer pNext;
	public boolean perViewPositionAllComponents;
}
