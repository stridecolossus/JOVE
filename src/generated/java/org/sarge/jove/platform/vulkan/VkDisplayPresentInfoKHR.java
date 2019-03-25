package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.VulkanBoolean;
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
	"srcRect",
	"dstRect",
	"persistent"
})
public class VkDisplayPresentInfoKHR extends VulkanStructure {
	public static class ByValue extends VkDisplayPresentInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkDisplayPresentInfoKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DISPLAY_PRESENT_INFO_KHR;
	public Pointer pNext;
	public VkRect2D srcRect;
	public VkRect2D dstRect;
	public VulkanBoolean persistent;
}