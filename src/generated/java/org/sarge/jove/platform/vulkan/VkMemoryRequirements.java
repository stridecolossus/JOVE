package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"size",
	"alignment",
	"memoryTypeBits"
})
public class VkMemoryRequirements extends VulkanStructure {
	public long size;
	public long alignment;
	public int memoryTypeBits;
}
