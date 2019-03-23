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
	"flags",
	"bindingCount",
	"pBindings"
})
public class VkDescriptorSetLayoutCreateInfo extends VulkanStructure {
	public static class ByValue extends VkDescriptorSetLayoutCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorSetLayoutCreateInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public int bindingCount;
	public Pointer pBindings;
}
