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
	"vertexAttributeInstanceRateDivisor",
	"vertexAttributeInstanceRateZeroDivisor"
})
public class VkPhysicalDeviceVertexAttributeDivisorFeaturesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceVertexAttributeDivisorFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceVertexAttributeDivisorFeaturesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VERTEX_ATTRIBUTE_DIVISOR_FEATURES_EXT;
	public Pointer pNext;
	public VulkanBoolean vertexAttributeInstanceRateDivisor;
	public VulkanBoolean vertexAttributeInstanceRateZeroDivisor;
}
