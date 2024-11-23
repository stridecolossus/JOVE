package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkCommandPoolCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.COMMAND_POOL_CREATE_INFO;
	public Handle pNext;
	public BitMask<VkCommandPoolCreateFlag> flags;
	public int queueFamilyIndex;
}
