package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"bindingCount",
	"pBindings"
})
public class VkDescriptorSetLayoutCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public int bindingCount;
	public VkDescriptorSetLayoutBinding pBindings;
}
