package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"maxSets",
	"poolSizeCount",
	"pPoolSizes"
})
public class VkDescriptorPoolCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DESCRIPTOR_POOL_CREATE_INFO;
	public Pointer pNext;
	public BitMask<VkDescriptorPoolCreateFlag> flags;
	public int maxSets;
	public int poolSizeCount;
	public VkDescriptorPoolSize pPoolSizes;
}
