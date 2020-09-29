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
	"imageFootprint"
})
public class VkPhysicalDeviceShaderImageFootprintFeaturesNV extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceShaderImageFootprintFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceShaderImageFootprintFeaturesNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SHADER_IMAGE_FOOTPRINT_FEATURES_NV;
	public Pointer pNext;
	public VulkanBoolean imageFootprint;
}
