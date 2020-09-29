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
	"shaderDrawParameters"
})
public class VkPhysicalDeviceShaderDrawParameterFeatures extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceShaderDrawParameterFeatures implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceShaderDrawParameterFeatures implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SHADER_DRAW_PARAMETER_FEATURES;
	public Pointer pNext;
	public VulkanBoolean shaderDrawParameters;
}
