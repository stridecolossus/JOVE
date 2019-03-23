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
	"shaderDrawParameters"
})
public class VkPhysicalDeviceShaderDrawParameterFeatures extends Structure {
	public static class ByValue extends VkPhysicalDeviceShaderDrawParameterFeatures implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceShaderDrawParameterFeatures implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SHADER_DRAW_PARAMETER_FEATURES.value();
	public Pointer pNext;
	public boolean shaderDrawParameters;
}
