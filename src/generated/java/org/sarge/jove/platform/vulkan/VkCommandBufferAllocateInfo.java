package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkCommandBufferAllocateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.COMMAND_BUFFER_ALLOCATE_INFO;
	public Handle pNext;
	public Handle commandPool;
	public VkCommandBufferLevel level;
	public int commandBufferCount;
}
