package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
	public int flags;
	public int maxSets;
	public int poolSizeCount;
	public VkDescriptorPoolSize pPoolSizes;
}
