package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

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
public class VkSurfaceCapabilitiesKHR extends Structure {
	public static class ByValue extends VkSurfaceCapabilitiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkSurfaceCapabilitiesKHR implements Structure.ByReference { }
	
	public int minImageCount;
	public int maxImageCount;
	public VkExtent2D currentExtent;
	public VkExtent2D minImageExtent;
	public VkExtent2D maxImageExtent;
	public int maxImageArrayLayers;
	public int supportedTransforms;
	public int currentTransform;
	public int supportedCompositeAlpha;
	public int supportedUsageFlags;
}
