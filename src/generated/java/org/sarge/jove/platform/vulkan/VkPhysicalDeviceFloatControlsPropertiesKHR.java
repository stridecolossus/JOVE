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

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_FLOAT_CONTROLS_PROPERTIES_KHR;
	public Pointer pNext;
	public boolean separateDenormSettings;
	public boolean separateRoundingModeSettings;
	public boolean shaderSignedZeroInfNanPreserveFloat16;
	public boolean shaderSignedZeroInfNanPreserveFloat32;
	public boolean shaderSignedZeroInfNanPreserveFloat64;
	public boolean shaderDenormPreserveFloat16;
	public boolean shaderDenormPreserveFloat32;
	public boolean shaderDenormPreserveFloat64;
	public boolean shaderDenormFlushToZeroFloat16;
	public boolean shaderDenormFlushToZeroFloat32;
	public boolean shaderDenormFlushToZeroFloat64;
	public boolean shaderRoundingModeRTEFloat16;
	public boolean shaderRoundingModeRTEFloat32;
	public boolean shaderRoundingModeRTEFloat64;
	public boolean shaderRoundingModeRTZFloat16;
	public boolean shaderRoundingModeRTZFloat32;
	public boolean shaderRoundingModeRTZFloat64;
}
