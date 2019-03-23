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
	"descriptorSetCount",
	"pDescriptorCounts"
})
public class VkDescriptorSetVariableDescriptorCountAllocateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkDescriptorSetVariableDescriptorCountAllocateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorSetVariableDescriptorCountAllocateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_VARIABLE_DESCRIPTOR_COUNT_ALLOCATE_INFO_EXT;
	public Pointer pNext;
	public int descriptorSetCount;
	public Pointer pDescriptorCounts;
}
