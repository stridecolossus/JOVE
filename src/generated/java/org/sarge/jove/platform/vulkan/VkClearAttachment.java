package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"aspectMask",
	"colorAttachment",
	"clearValue"
})
public class VkClearAttachment extends VulkanStructure {
	public static class ByValue extends VkClearAttachment implements Structure.ByValue { }
	public static class ByReference extends VkClearAttachment implements Structure.ByReference { }
	
	public VkImageAspectFlags aspectMask;
	public int colorAttachment;
	public VkClearValue clearValue;
}
