package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"image",
	"memory",
	"memoryOffset"
})
public class VkBindImageMemoryInfo extends VulkanStructure {
	public static class ByValue extends VkBindImageMemoryInfo implements Structure.ByValue { }
	public static class ByReference extends VkBindImageMemoryInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_BIND_IMAGE_MEMORY_INFO;
	public Pointer pNext;
	public Pointer image;
	public Pointer memory;
	public long memoryOffset;
}
