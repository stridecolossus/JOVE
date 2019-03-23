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
	"shaderFloat16",
	"shaderInt8"
})
public class VkPhysicalDeviceFloat16Int8FeaturesKHR extends Structure {
	public static class ByValue extends VkPhysicalDeviceFloat16Int8FeaturesKHR implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceFloat16Int8FeaturesKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FLOAT_16_INT_8_FEATURES_KHR.value();
	public Pointer pNext;
	public boolean shaderFloat16;
	public boolean shaderInt8;
}
