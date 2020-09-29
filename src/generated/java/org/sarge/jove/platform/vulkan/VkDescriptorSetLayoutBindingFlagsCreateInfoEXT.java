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
	"bindingCount",
	"pBindingFlags"
})
public class VkDescriptorSetLayoutBindingFlagsCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkDescriptorSetLayoutBindingFlagsCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorSetLayoutBindingFlagsCreateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_BINDING_FLAGS_CREATE_INFO_EXT;
	public Pointer pNext;
	public int bindingCount;
	public Pointer pBindingFlags;
}
