package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"surface",
	"minImageCount",
	"imageFormat",
	"imageColorSpace",
	"imageExtent",
	"imageArrayLayers",
	"imageUsage",
	"imageSharingMode",
	"queueFamilyIndexCount",
	"pQueueFamilyIndices",
	"preTransform",
	"compositeAlpha",
	"presentMode",
	"clipped",
	"oldSwapchain"
})
public class VkSwapchainCreateInfoKHR extends VulkanStructure {
	public VkStructureType sType = VkStructureType.SWAPCHAIN_CREATE_INFO_KHR;
	public Pointer pNext;
	public BitMask<VkSwapchainCreateFlagKHR> flags;
	public Handle surface;
	public int minImageCount;
	public VkFormat imageFormat;
	public VkColorSpaceKHR imageColorSpace;
	public VkExtent2D imageExtent;
	public int imageArrayLayers;
	public BitMask<VkImageUsageFlag> imageUsage;
	public VkSharingMode imageSharingMode;
	public int queueFamilyIndexCount;
	public Pointer pQueueFamilyIndices;
	public VkSurfaceTransformFlagKHR preTransform;
	public VkCompositeAlphaFlagKHR compositeAlpha;
	public VkPresentModeKHR presentMode;
	public boolean clipped;
	public Pointer oldSwapchain;
}
