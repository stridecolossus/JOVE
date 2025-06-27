package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkBufferMemoryBarrier extends VulkanStructure {
	public final VkStructureType sType = VkStructureType.BUFFER_MEMORY_BARRIER;
	public Handle pNext;
	public EnumMask<VkAccess> srcAccessMask;
	public EnumMask<VkAccess> dstAccessMask;
	public int srcQueueFamilyIndex;
	public int dstQueueFamilyIndex;
	public Handle buffer;
	public long offset;
	public long size;
}
