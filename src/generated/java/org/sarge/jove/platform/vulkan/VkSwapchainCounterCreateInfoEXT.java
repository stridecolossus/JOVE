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
	"surfaceCounters"
})
public class VkSwapchainCounterCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkSwapchainCounterCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkSwapchainCounterCreateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_SWAPCHAIN_COUNTER_CREATE_INFO_EXT;
	public Pointer pNext;
	public VkSurfaceCounterFlagsEXT surfaceCounters;
}