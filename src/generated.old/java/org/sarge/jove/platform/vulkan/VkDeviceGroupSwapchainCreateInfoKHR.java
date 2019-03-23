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
	"modes"
})
public class VkDeviceGroupSwapchainCreateInfoKHR extends Structure {
	public static class ByValue extends VkDeviceGroupSwapchainCreateInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkDeviceGroupSwapchainCreateInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_GROUP_SWAPCHAIN_CREATE_INFO_KHR.value();
	public Pointer pNext;
	public int modes;
}
