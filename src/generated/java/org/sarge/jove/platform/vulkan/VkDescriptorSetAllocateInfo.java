package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkDescriptorSetAllocateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DESCRIPTOR_SET_ALLOCATE_INFO;
	public Handle pNext;
	public Handle descriptorPool;
	public int descriptorSetCount;
	public Handle[] pSetLayouts;
}
