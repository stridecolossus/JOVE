package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkCommandBufferBeginInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.COMMAND_BUFFER_BEGIN_INFO;
	public Handle pNext;
	public BitMask<VkCommandBufferUsage> flags;
	public VkCommandBufferInheritanceInfo pInheritanceInfo;
}
