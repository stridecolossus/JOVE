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
	"image",
	"memory",
	"memoryOffset"
})
public class VkBindImageMemoryInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.BIND_IMAGE_MEMORY_INFO;
	public Pointer pNext;
	public Pointer image;
	public Pointer memory;
	public long memoryOffset;
}
