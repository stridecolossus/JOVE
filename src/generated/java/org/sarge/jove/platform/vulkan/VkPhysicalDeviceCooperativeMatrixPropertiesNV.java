package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"cooperativeMatrixSupportedStages"
})
public class VkPhysicalDeviceCooperativeMatrixPropertiesNV extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceCooperativeMatrixPropertiesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceCooperativeMatrixPropertiesNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_COOPERATIVE_MATRIX_PROPERTIES_NV;
	public Pointer pNext;
	public VkShaderStageFlags cooperativeMatrixSupportedStages;
}
