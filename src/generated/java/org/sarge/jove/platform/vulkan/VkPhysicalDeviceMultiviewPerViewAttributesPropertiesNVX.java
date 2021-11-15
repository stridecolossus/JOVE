package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

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
	"perViewPositionAllComponents"
})
public class VkPhysicalDeviceMultiviewPerViewAttributesPropertiesNVX extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceMultiviewPerViewAttributesPropertiesNVX implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMultiviewPerViewAttributesPropertiesNVX implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_MULTIVIEW_PER_VIEW_ATTRIBUTES_PROPERTIES_NVX;
	public Pointer pNext;
	public VulkanBoolean perViewPositionAllComponents;
}
