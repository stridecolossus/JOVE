package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"srcOffset",
	"dstOffset",
	"size"
})
public class VkBufferCopy extends VulkanStructure {
	public long srcOffset;
	public long dstOffset;
	public long size;
}
