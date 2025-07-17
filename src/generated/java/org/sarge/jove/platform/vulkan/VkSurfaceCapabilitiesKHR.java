package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkSurfaceCapabilitiesKHR extends VulkanStructure {
	public int minImageCount;
	public int maxImageCount;
	public VkExtent2D currentExtent;
	public VkExtent2D minImageExtent;
	public VkExtent2D maxImageExtent;
	public int maxImageArrayLayers;
	public EnumMask<VkSurfaceTransformFlagKHR> supportedTransforms;
	public VkSurfaceTransformFlagKHR currentTransform;
	public EnumMask<VkCompositeAlphaFlagKHR> supportedCompositeAlpha;
	public EnumMask<VkImageUsageFlag> supportedUsageFlags;
}
