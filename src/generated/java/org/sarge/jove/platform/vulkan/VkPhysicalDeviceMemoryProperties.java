package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"memoryTypeCount",
	"memoryTypes",
	"memoryHeapCount",
	"memoryHeaps"
})
public class VkPhysicalDeviceMemoryProperties extends VulkanStructure {
	public int memoryTypeCount;
	public VkMemoryType[] memoryTypes = new VkMemoryType[32];
	public int memoryHeapCount;
	public VkMemoryHeap[] memoryHeaps = new VkMemoryHeap[16];
}
