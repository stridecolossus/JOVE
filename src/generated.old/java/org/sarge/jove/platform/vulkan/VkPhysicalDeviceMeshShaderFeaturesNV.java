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
	"taskShader",
	"meshShader"
})
public class VkPhysicalDeviceMeshShaderFeaturesNV extends Structure {
	public static class ByValue extends VkPhysicalDeviceMeshShaderFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMeshShaderFeaturesNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MESH_SHADER_FEATURES_NV.value();
	public Pointer pNext;
	public boolean taskShader;
	public boolean meshShader;
}
