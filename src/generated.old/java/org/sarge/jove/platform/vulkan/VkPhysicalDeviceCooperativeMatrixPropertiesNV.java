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
	"cooperativeMatrixSupportedStages"
})
public class VkPhysicalDeviceCooperativeMatrixPropertiesNV extends Structure {
	public static class ByValue extends VkPhysicalDeviceCooperativeMatrixPropertiesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceCooperativeMatrixPropertiesNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_COOPERATIVE_MATRIX_PROPERTIES_NV.value();
	public Pointer pNext;
	public int cooperativeMatrixSupportedStages;
}
