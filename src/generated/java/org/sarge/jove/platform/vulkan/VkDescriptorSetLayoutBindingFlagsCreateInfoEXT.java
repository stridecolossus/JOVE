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
	"bindingCount",
	"pBindingFlags"
})
public class VkDescriptorSetLayoutBindingFlagsCreateInfoEXT extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DESCRIPTOR_SET_LAYOUT_BINDING_FLAGS_CREATE_INFO_EXT;
	public Pointer pNext;
	public int bindingCount;
	public Pointer pBindingFlags;
}
