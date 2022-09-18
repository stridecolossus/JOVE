package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	public VkImageAspect aspectMask;
	public int colorAttachment;
	public VkClearValue clearValue;
}
