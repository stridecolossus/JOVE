package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"rect",
	"baseArrayLayer",
	"layerCount"
})
public class VkClearRect extends VulkanStructure implements ByReference {
	public VkRect2D rect;
	public int baseArrayLayer;
	public int layerCount;
}
