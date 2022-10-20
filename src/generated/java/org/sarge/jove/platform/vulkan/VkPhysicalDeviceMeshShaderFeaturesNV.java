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
	"taskShader",
	"meshShader"
})
public class VkPhysicalDeviceMeshShaderFeaturesNV extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceMeshShaderFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMeshShaderFeaturesNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_MESH_SHADER_FEATURES_NV;
	public Pointer pNext;
	public boolean taskShader;
	public boolean meshShader;
}
