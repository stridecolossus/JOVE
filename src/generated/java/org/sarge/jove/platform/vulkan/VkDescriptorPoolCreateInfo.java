package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkDescriptorPoolCreateInfo extends VulkanStructure {
	public final VkStructureType sType = VkStructureType.DESCRIPTOR_POOL_CREATE_INFO;
	public Handle pNext;
	public EnumMask<VkDescriptorPoolCreateFlag> flags;
	public int maxSets;
	public int poolSizeCount;
	public VkDescriptorPoolSize[] pPoolSizes;
}
