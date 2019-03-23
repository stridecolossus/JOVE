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
	"imageFootprint"
})
public class VkPhysicalDeviceShaderImageFootprintFeaturesNV extends Structure {
	public static class ByValue extends VkPhysicalDeviceShaderImageFootprintFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceShaderImageFootprintFeaturesNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SHADER_IMAGE_FOOTPRINT_FEATURES_NV.value();
	public Pointer pNext;
	public boolean imageFootprint;
}
