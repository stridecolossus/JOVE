package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

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
public class VkPhysicalDeviceComputeShaderDerivativesFeaturesNV extends Structure {
	public static class ByValue extends VkPhysicalDeviceComputeShaderDerivativesFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceComputeShaderDerivativesFeaturesNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_COMPUTE_SHADER_DERIVATIVES_FEATURES_NV.value();
	public Pointer pNext;
	public boolean computeDerivativeGroupQuads;
	public boolean computeDerivativeGroupLinear;
}
