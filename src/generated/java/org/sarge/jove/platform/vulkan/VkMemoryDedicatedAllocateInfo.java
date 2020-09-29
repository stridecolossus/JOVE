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
	"buffer"
})
public class VkMemoryDedicatedAllocateInfo extends VulkanStructure {
	public static class ByValue extends VkMemoryDedicatedAllocateInfo implements Structure.ByValue { }
	public static class ByReference extends VkMemoryDedicatedAllocateInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_MEMORY_DEDICATED_ALLOCATE_INFO;
	public Pointer pNext;
	public Pointer image;
	public Pointer buffer;
}
