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
	"swapchainCount",
	"pRegions"
})
public class VkPresentRegionsKHR extends Structure {
	public static class ByValue extends VkPresentRegionsKHR implements Structure.ByValue { }
	public static class ByReference extends VkPresentRegionsKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PRESENT_REGIONS_KHR.value();
	public Pointer pNext;
	public int swapchainCount;
	public VkPresentRegionKHR.ByReference pRegions;
}
