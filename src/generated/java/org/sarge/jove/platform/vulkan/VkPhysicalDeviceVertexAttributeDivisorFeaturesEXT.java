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
	"vertexAttributeInstanceRateDivisor",
	"vertexAttributeInstanceRateZeroDivisor"
})
public class VkPhysicalDeviceVertexAttributeDivisorFeaturesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceVertexAttributeDivisorFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceVertexAttributeDivisorFeaturesEXT implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_VERTEX_ATTRIBUTE_DIVISOR_FEATURES_EXT;
	public Pointer pNext;
	public boolean vertexAttributeInstanceRateDivisor;
	public boolean vertexAttributeInstanceRateZeroDivisor;
}
