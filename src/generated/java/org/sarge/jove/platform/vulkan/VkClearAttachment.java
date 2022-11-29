package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Structure.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"aspectMask",
	"colorAttachment",
	"clearValue"
})
public class VkClearAttachment extends VulkanStructure implements ByReference {
	public BitMask<VkImageAspect> aspectMask;
	public int colorAttachment;
	public VkClearValue clearValue;
}
