package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
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
	public VkStructureType sType = VkStructureType.DISPLAY_PRESENT_INFO_KHR;
	public Pointer pNext;
	public VkRect2D srcRect;
	public VkRect2D dstRect;
	public boolean persistent;
}
