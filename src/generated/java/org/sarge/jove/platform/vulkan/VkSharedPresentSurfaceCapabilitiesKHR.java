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
	"sharedPresentSupportedUsageFlags"
})
public class VkSharedPresentSurfaceCapabilitiesKHR extends VulkanStructure {
	public static class ByValue extends VkSharedPresentSurfaceCapabilitiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkSharedPresentSurfaceCapabilitiesKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_SHARED_PRESENT_SURFACE_CAPABILITIES_KHR;
	public Pointer pNext;
	public VkImageUsageFlags sharedPresentSupportedUsageFlags;
}
