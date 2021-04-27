package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
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
	"shaderFloat16",
	"shaderInt8"
})
public class VkPhysicalDeviceFloat16Int8FeaturesKHR extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceFloat16Int8FeaturesKHR implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceFloat16Int8FeaturesKHR implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FLOAT16_INT8_FEATURES_KHR;
	public Pointer pNext;
	public VulkanBoolean shaderFloat16;
	public VulkanBoolean shaderInt8;
}
