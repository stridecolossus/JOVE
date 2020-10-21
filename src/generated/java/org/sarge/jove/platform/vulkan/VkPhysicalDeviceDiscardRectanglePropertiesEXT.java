package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"maxDiscardRectangles"
})
public class VkPhysicalDeviceDiscardRectanglePropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceDiscardRectanglePropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceDiscardRectanglePropertiesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_DISCARD_RECTANGLE_PROPERTIES_EXT;
	public Pointer pNext;
	public int maxDiscardRectangles;
}
