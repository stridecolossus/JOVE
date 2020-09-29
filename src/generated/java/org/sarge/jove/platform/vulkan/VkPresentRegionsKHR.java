package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"swapchainCount",
	"pRegions"
})
public class VkPresentRegionsKHR extends VulkanStructure {
	public static class ByValue extends VkPresentRegionsKHR implements Structure.ByValue { }
	public static class ByReference extends VkPresentRegionsKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PRESENT_REGIONS_KHR;
	public Pointer pNext;
	public int swapchainCount;
	public Pointer pRegions;
}
