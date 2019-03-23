package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"size",
	"flags"
})
public class VkMemoryHeap extends VulkanStructure {
	public static class ByValue extends VkMemoryHeap implements Structure.ByValue { }
	public static class ByReference extends VkMemoryHeap implements Structure.ByReference { }
	
	public long size;
	public int flags;
}
