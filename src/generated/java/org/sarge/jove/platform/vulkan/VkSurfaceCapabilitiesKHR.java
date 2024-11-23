package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

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
	public BitMask<VkSurfaceTransformFlagKHR> supportedTransforms;
	public VkSurfaceTransformFlagKHR currentTransform;
	public BitMask<VkCompositeAlphaFlagKHR> supportedCompositeAlpha;
	public BitMask<VkImageUsageFlag> supportedUsageFlags;
}
