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
	"vertexAttributeInstanceRateDivisor",
	"vertexAttributeInstanceRateZeroDivisor"
})
public class VkPhysicalDeviceVertexAttributeDivisorFeaturesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceVertexAttributeDivisorFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceVertexAttributeDivisorFeaturesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VERTEX_ATTRIBUTE_DIVISOR_FEATURES_EXT.value();
	public Pointer pNext;
	public boolean vertexAttributeInstanceRateDivisor;
	public boolean vertexAttributeInstanceRateZeroDivisor;
}
