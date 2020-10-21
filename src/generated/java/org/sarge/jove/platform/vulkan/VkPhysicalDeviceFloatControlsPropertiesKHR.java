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
	"separateDenormSettings",
	"separateRoundingModeSettings",
	"shaderSignedZeroInfNanPreserveFloat16",
	"shaderSignedZeroInfNanPreserveFloat32",
	"shaderSignedZeroInfNanPreserveFloat64",
	"shaderDenormPreserveFloat16",
	"shaderDenormPreserveFloat32",
	"shaderDenormPreserveFloat64",
	"shaderDenormFlushToZeroFloat16",
	"shaderDenormFlushToZeroFloat32",
	"shaderDenormFlushToZeroFloat64",
	"shaderRoundingModeRTEFloat16",
	"shaderRoundingModeRTEFloat32",
	"shaderRoundingModeRTEFloat64",
	"shaderRoundingModeRTZFloat16",
	"shaderRoundingModeRTZFloat32",
	"shaderRoundingModeRTZFloat64"
})
public class VkPhysicalDeviceFloatControlsPropertiesKHR extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceFloatControlsPropertiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceFloatControlsPropertiesKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FLOAT_CONTROLS_PROPERTIES_KHR;
	public Pointer pNext;
	public VulkanBoolean separateDenormSettings;
	public VulkanBoolean separateRoundingModeSettings;
	public VulkanBoolean shaderSignedZeroInfNanPreserveFloat16;
	public VulkanBoolean shaderSignedZeroInfNanPreserveFloat32;
	public VulkanBoolean shaderSignedZeroInfNanPreserveFloat64;
	public VulkanBoolean shaderDenormPreserveFloat16;
	public VulkanBoolean shaderDenormPreserveFloat32;
	public VulkanBoolean shaderDenormPreserveFloat64;
	public VulkanBoolean shaderDenormFlushToZeroFloat16;
	public VulkanBoolean shaderDenormFlushToZeroFloat32;
	public VulkanBoolean shaderDenormFlushToZeroFloat64;
	public VulkanBoolean shaderRoundingModeRTEFloat16;
	public VulkanBoolean shaderRoundingModeRTEFloat32;
	public VulkanBoolean shaderRoundingModeRTEFloat64;
	public VulkanBoolean shaderRoundingModeRTZFloat16;
	public VulkanBoolean shaderRoundingModeRTZFloat32;
	public VulkanBoolean shaderRoundingModeRTZFloat64;
}
