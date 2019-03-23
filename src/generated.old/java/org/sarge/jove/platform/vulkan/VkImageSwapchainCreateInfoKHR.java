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
	"swapchain"
})
public class VkImageSwapchainCreateInfoKHR extends Structure {
	public static class ByValue extends VkImageSwapchainCreateInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkImageSwapchainCreateInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_IMAGE_SWAPCHAIN_CREATE_INFO_KHR.value();
	public Pointer pNext;
	public long swapchain;
}
