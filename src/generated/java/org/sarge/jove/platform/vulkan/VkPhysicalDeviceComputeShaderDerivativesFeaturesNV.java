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
	"computeDerivativeGroupQuads",
	"computeDerivativeGroupLinear"
})
public class VkPhysicalDeviceComputeShaderDerivativesFeaturesNV extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceComputeShaderDerivativesFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceComputeShaderDerivativesFeaturesNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_COMPUTE_SHADER_DERIVATIVES_FEATURES_NV;
	public Pointer pNext;
	public VulkanBoolean computeDerivativeGroupQuads;
	public VulkanBoolean computeDerivativeGroupLinear;
}
