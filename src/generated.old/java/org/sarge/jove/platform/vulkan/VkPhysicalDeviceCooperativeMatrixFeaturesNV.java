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
	"cooperativeMatrix",
	"cooperativeMatrixRobustBufferAccess"
})
public class VkPhysicalDeviceCooperativeMatrixFeaturesNV extends Structure {
	public static class ByValue extends VkPhysicalDeviceCooperativeMatrixFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceCooperativeMatrixFeaturesNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_COOPERATIVE_MATRIX_FEATURES_NV.value();
	public Pointer pNext;
	public boolean cooperativeMatrix;
	public boolean cooperativeMatrixRobustBufferAccess;
}
