package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

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
	"fragmentShaderBarycentric"
})
public class VkPhysicalDeviceFragmentShaderBarycentricFeaturesNV extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceFragmentShaderBarycentricFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceFragmentShaderBarycentricFeaturesNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_FRAGMENT_SHADER_BARYCENTRIC_FEATURES_NV;
	public Pointer pNext;
	public VulkanBoolean fragmentShaderBarycentric;
}
