package org.sarge.jove.platform.vulkan;

import java.util.Collection;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkDescriptorSetLayoutCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
	public Handle pNext;
	public BitMask<VkDescriptorSetLayoutCreateFlag> flags;
	public int bindingCount;
	public Collection<VkDescriptorSetLayoutBinding> pBindings;
}
