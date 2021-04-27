package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
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
	"conditionalRendering",
	"inheritedConditionalRendering"
})
public class VkPhysicalDeviceConditionalRenderingFeaturesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceConditionalRenderingFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceConditionalRenderingFeaturesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_CONDITIONAL_RENDERING_FEATURES_EXT;
	public Pointer pNext;
	public VulkanBoolean conditionalRendering;
	public VulkanBoolean inheritedConditionalRendering;
}
