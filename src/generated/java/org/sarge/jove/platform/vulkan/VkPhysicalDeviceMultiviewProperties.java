package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"maxMultiviewViewCount",
	"maxMultiviewInstanceIndex"
})
public class VkPhysicalDeviceMultiviewProperties extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceMultiviewProperties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMultiviewProperties implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_MULTIVIEW_PROPERTIES;
	public Pointer pNext;
	public int maxMultiviewViewCount;
	public int maxMultiviewInstanceIndex;
}
