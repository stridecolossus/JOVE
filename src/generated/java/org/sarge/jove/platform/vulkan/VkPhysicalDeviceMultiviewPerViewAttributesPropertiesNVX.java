package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.*;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"perViewPositionAllComponents"
})
public class VkPhysicalDeviceMultiviewPerViewAttributesPropertiesNVX extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceMultiviewPerViewAttributesPropertiesNVX implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMultiviewPerViewAttributesPropertiesNVX implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_MULTIVIEW_PER_VIEW_ATTRIBUTES_PROPERTIES_NVX;
	public Pointer pNext;
	public boolean perViewPositionAllComponents;
}
