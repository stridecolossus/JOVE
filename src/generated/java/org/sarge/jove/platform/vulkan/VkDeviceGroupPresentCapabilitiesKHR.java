package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
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
	public VkStructureType sType = VkStructureType.DEVICE_GROUP_PRESENT_CAPABILITIES_KHR;
	public Pointer pNext;
	public int[] presentMask = new int[32];
	public VkDeviceGroupPresentModeFlagKHR modes;
}
