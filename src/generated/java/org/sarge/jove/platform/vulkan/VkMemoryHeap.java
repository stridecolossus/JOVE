package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

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
	public long size;
	public BitMask<VkMemoryHeapFlag> flags;
}
