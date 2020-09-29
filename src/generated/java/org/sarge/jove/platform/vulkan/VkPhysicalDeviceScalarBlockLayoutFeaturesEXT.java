package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

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
	"scalarBlockLayout"
})
public class VkPhysicalDeviceScalarBlockLayoutFeaturesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceScalarBlockLayoutFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceScalarBlockLayoutFeaturesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SCALAR_BLOCK_LAYOUT_FEATURES_EXT;
	public Pointer pNext;
	public VulkanBoolean scalarBlockLayout;
}
