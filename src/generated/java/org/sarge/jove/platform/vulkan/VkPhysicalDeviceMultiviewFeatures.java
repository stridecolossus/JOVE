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
	"multiview",
	"multiviewGeometryShader",
	"multiviewTessellationShader"
})
public class VkPhysicalDeviceMultiviewFeatures extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceMultiviewFeatures implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMultiviewFeatures implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_MULTIVIEW_FEATURES;
	public Pointer pNext;
	public boolean multiview;
	public boolean multiviewGeometryShader;
	public boolean multiviewTessellationShader;
}
