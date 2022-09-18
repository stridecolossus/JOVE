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
	"descriptorUpdateEntryCount",
	"pDescriptorUpdateEntries",
	"templateType",
	"descriptorSetLayout",
	"pipelineBindPoint",
	"pipelineLayout",
	"set"
})
public class VkDescriptorUpdateTemplateCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DESCRIPTOR_UPDATE_TEMPLATE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public int descriptorUpdateEntryCount;
	public Pointer pDescriptorUpdateEntries;
	public VkDescriptorUpdateTemplateType templateType;
	public Pointer descriptorSetLayout;
	public VkPipelineBindPoint pipelineBindPoint;
	public Pointer pipelineLayout;
	public int set;
}
