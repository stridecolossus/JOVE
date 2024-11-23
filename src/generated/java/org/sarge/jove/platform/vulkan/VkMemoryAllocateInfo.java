package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkMemoryAllocateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.MEMORY_ALLOCATE_INFO;
	public Handle pNext;
	public long allocationSize;
	public int memoryTypeIndex;
}
