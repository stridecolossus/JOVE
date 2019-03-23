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
	"fragmentShaderBarycentric"
})
public class VkPhysicalDeviceFragmentShaderBarycentricFeaturesNV extends Structure {
	public static class ByValue extends VkPhysicalDeviceFragmentShaderBarycentricFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceFragmentShaderBarycentricFeaturesNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FRAGMENT_SHADER_BARYCENTRIC_FEATURES_NV.value();
	public Pointer pNext;
	public boolean fragmentShaderBarycentric;
}
