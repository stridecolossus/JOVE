package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkFenceCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.FENCE_CREATE_INFO;
	public Handle pNext;
	public BitMask<VkFenceCreateFlag> flags;
}
