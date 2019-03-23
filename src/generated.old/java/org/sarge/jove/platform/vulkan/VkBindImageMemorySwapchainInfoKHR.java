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
	"swapchain",
	"imageIndex"
})
public class VkBindImageMemorySwapchainInfoKHR extends Structure {
	public static class ByValue extends VkBindImageMemorySwapchainInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkBindImageMemorySwapchainInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_BIND_IMAGE_MEMORY_SWAPCHAIN_INFO_KHR.value();
	public Pointer pNext;
	public long swapchain;
	public int imageIndex;
}
