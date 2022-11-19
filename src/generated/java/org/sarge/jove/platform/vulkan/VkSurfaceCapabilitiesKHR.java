package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Structure.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"minImageCount",
	"maxImageCount",
	"currentExtent",
	"minImageExtent",
	"maxImageExtent",
	"maxImageArrayLayers",
	"supportedTransforms",
	"currentTransform",
	"supportedCompositeAlpha",
	"supportedUsageFlags"
})
public class VkSurfaceCapabilitiesKHR extends VulkanStructure implements ByReference {
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
