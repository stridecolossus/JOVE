package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkMemoryBarrier extends VulkanStructure {
	public VkStructureType sType = VkStructureType.MEMORY_BARRIER;
	public Handle pNext;
	public EnumMask<VkAccess> srcAccessMask;
	public EnumMask<VkAccess> dstAccessMask;
}
