package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import com.sun.jna.Pointer;

import com.sun.jna.Structure;
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
	public static class ByValue extends VkDescriptorSetAllocateInfo implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorSetAllocateInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
	public Pointer pNext;
	public Pointer descriptorPool;
	public int descriptorSetCount;
	public Pointer pSetLayouts;
}