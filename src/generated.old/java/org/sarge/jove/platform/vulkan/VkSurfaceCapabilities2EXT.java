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
public class VkSurfaceCapabilities2EXT extends Structure {
	public static class ByValue extends VkSurfaceCapabilities2EXT implements Structure.ByValue { }
	public static class ByReference extends VkSurfaceCapabilities2EXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SURFACE_CAPABILITIES_2_EXT.value();
	public Pointer pNext;
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
	public int supportedSurfaceCounters;
}
