package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"presentMask",
	"modes"
})
public class VkDeviceGroupPresentCapabilitiesKHR extends VulkanStructure {
	public static class ByValue extends VkDeviceGroupPresentCapabilitiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkDeviceGroupPresentCapabilitiesKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_GROUP_PRESENT_CAPABILITIES_KHR;
	public Pointer pNext;
	public int[] presentMask = new int[32];
	public VkDeviceGroupPresentModeFlagsKHR modes;
}
