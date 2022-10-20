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
	"computeDerivativeGroupQuads",
	"computeDerivativeGroupLinear"
})
public class VkPhysicalDeviceComputeShaderDerivativesFeaturesNV extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceComputeShaderDerivativesFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceComputeShaderDerivativesFeaturesNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_COMPUTE_SHADER_DERIVATIVES_FEATURES_NV;
	public Pointer pNext;
	public boolean computeDerivativeGroupQuads;
	public boolean computeDerivativeGroupLinear;
}
