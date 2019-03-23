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
	"minImageCount",
	"maxImageCount",
	"currentExtent",
	"minImageExtent",
	"maxImageExtent",
	"maxImageArrayLayers",
	"supportedTransforms",
	"currentTransform",
	"supportedCompositeAlpha",
	"supportedUsageFlags",
	"supportedSurfaceCounters"
})
public class VkSurfaceCapabilities2EXT extends VulkanStructure {
	public static class ByValue extends VkSurfaceCapabilities2EXT implements Structure.ByValue { }
	public static class ByReference extends VkSurfaceCapabilities2EXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_SURFACE_CAPABILITIES_2_EXT;
	public Pointer pNext;
	public int minImageCount;
	public int maxImageCount;
	public VkExtent2D currentExtent;
	public VkExtent2D minImageExtent;
	public VkExtent2D maxImageExtent;
	public int maxImageArrayLayers;
	public VkSurfaceTransformFlagsKHR supportedTransforms;
	public VkSurfaceTransformFlagKHR currentTransform;
	public VkCompositeAlphaFlagsKHR supportedCompositeAlpha;
	public VkImageUsageFlags supportedUsageFlags;
	public VkSurfaceCounterFlagsEXT supportedSurfaceCounters;
}
