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
	"memory",
	"offset",
	"size"
})
public class VkMappedMemoryRange extends VulkanStructure {
	public static class ByValue extends VkMappedMemoryRange implements Structure.ByValue { }
	public static class ByReference extends VkMappedMemoryRange implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE;
	public Pointer pNext;
	public Pointer memory;
	public long offset;
	public long size;
}
