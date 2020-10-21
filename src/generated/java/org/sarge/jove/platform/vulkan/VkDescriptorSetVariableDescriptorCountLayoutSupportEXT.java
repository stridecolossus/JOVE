package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"maxVariableDescriptorCount"
})
public class VkDescriptorSetVariableDescriptorCountLayoutSupportEXT extends VulkanStructure {
	public static class ByValue extends VkDescriptorSetVariableDescriptorCountLayoutSupportEXT implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorSetVariableDescriptorCountLayoutSupportEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_VARIABLE_DESCRIPTOR_COUNT_LAYOUT_SUPPORT_EXT;
	public Pointer pNext;
	public int maxVariableDescriptorCount;
}
