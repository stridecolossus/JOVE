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
	"advancedBlendCoherentOperations"
})
public class VkPhysicalDeviceBlendOperationAdvancedFeaturesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceBlendOperationAdvancedFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceBlendOperationAdvancedFeaturesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_BLEND_OPERATION_ADVANCED_FEATURES_EXT.value();
	public Pointer pNext;
	public boolean advancedBlendCoherentOperations;
}
