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
	"swapchainCount",
	"pDeviceMasks",
	"mode"
})
public class VkDeviceGroupPresentInfoKHR extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DEVICE_GROUP_PRESENT_INFO_KHR;
	public Pointer pNext;
	public int swapchainCount;
	public Pointer pDeviceMasks;
	public VkDeviceGroupPresentModeFlagKHR mode;
}
