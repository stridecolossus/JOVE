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
	"cooperativeMatrix",
	"cooperativeMatrixRobustBufferAccess"
})
public class VkPhysicalDeviceCooperativeMatrixFeaturesNV extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceCooperativeMatrixFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceCooperativeMatrixFeaturesNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_COOPERATIVE_MATRIX_FEATURES_NV;
	public Pointer pNext;
	public boolean cooperativeMatrix;
	public boolean cooperativeMatrixRobustBufferAccess;
}
