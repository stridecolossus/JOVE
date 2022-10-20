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
	"imageFootprint"
})
public class VkPhysicalDeviceShaderImageFootprintFeaturesNV extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceShaderImageFootprintFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceShaderImageFootprintFeaturesNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_SHADER_IMAGE_FOOTPRINT_FEATURES_NV;
	public Pointer pNext;
	public boolean imageFootprint;
}
