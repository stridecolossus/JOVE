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
	"shaderFloat16",
	"shaderInt8"
})
public class VkPhysicalDeviceFloat16Int8FeaturesKHR extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceFloat16Int8FeaturesKHR implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceFloat16Int8FeaturesKHR implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_FLOAT16_INT8_FEATURES_KHR;
	public Pointer pNext;
	public boolean shaderFloat16;
	public boolean shaderInt8;
}
