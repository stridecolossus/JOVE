package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

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
public class VkDescriptorSetVariableDescriptorCountAllocateInfoEXT extends Structure {
	public static class ByValue extends VkDescriptorSetVariableDescriptorCountAllocateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorSetVariableDescriptorCountAllocateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_VARIABLE_DESCRIPTOR_COUNT_ALLOCATE_INFO_EXT.value();
	public Pointer pNext;
	public int descriptorSetCount;
	public int pDescriptorCounts;
}
