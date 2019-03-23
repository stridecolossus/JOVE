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
	"conditionalRendering",
	"inheritedConditionalRendering"
})
public class VkPhysicalDeviceConditionalRenderingFeaturesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceConditionalRenderingFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceConditionalRenderingFeaturesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_CONDITIONAL_RENDERING_FEATURES_EXT.value();
	public Pointer pNext;
	public boolean conditionalRendering;
	public boolean inheritedConditionalRendering;
}
