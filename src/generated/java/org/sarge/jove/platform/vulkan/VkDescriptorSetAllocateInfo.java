package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
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
	"descriptorPool",
	"descriptorSetCount",
	"pSetLayouts"
})
public class VkDescriptorSetAllocateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DESCRIPTOR_SET_ALLOCATE_INFO;
	public Pointer pNext;
	public Handle descriptorPool;
	public int descriptorSetCount;
	public Pointer pSetLayouts;
}
