package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.platform.vulkan.render.Swapchain;
import org.sarge.jove.util.BitMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkSwapchainCreateInfoKHR extends VulkanStructure {
	public VkStructureType sType = VkStructureType.SWAPCHAIN_CREATE_INFO_KHR;
	public Handle pNext;
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
	public int[] pQueueFamilyIndices;
	public VkSurfaceTransformFlagKHR preTransform;
	public VkCompositeAlphaFlagKHR compositeAlpha;
	public VkPresentModeKHR presentMode;
	public boolean clipped;
	public Swapchain oldSwapchain;
}
