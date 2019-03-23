package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

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
	public static class ByValue extends VkSwapchainCreateInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkSwapchainCreateInfoKHR implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR.value();
	public Pointer pNext;
	public int flags;
	public Pointer surface;
	public int minImageCount;
	public VkFormat imageFormat = VkFormat.VK_FORMAT_UNDEFINED;
	public VkColorSpaceKHR imageColorSpace;
	public VkExtent2D imageExtent;
	public int imageArrayLayers;
	public VkImageUsageFlag imageUsage;
	public VkSharingMode imageSharingMode;
	public int queueFamilyIndexCount;
	public Pointer pQueueFamilyIndices;
	public VkSurfaceTransformFlagBitsKHR preTransform = VkSurfaceTransformFlagBitsKHR.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
	public VkCompositeAlphaFlagBitsKHR compositeAlpha;
	public VkPresentModeKHR presentMode;
	public VulkanBoolean clipped = VulkanBoolean.FALSE;
	public Pointer oldSwapchain;
}
