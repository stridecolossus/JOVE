package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.VulkanBoolean;
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
	"multiview",
	"multiviewGeometryShader",
	"multiviewTessellationShader"
})
public class VkPhysicalDeviceMultiviewFeatures extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceMultiviewFeatures implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMultiviewFeatures implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MULTIVIEW_FEATURES;
	public Pointer pNext;
	public VulkanBoolean multiview;
	public VulkanBoolean multiviewGeometryShader;
	public VulkanBoolean multiviewTessellationShader;
}
