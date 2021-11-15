package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"stageFlags",
	"offset",
	"size"
})
public class VkPushConstantRange extends VulkanStructure {
	public int stageFlags;
	public int offset;
	public int size;
}
