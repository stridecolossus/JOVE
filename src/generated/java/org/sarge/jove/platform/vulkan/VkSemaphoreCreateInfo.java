package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkSemaphoreCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.SEMAPHORE_CREATE_INFO;
	public Handle pNext;
	public int flags;
}
