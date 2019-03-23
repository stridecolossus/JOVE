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
	"flags",
	"descriptorUpdateEntryCount",
	"pDescriptorUpdateEntries",
	"templateType",
	"descriptorSetLayout",
	"pipelineBindPoint",
	"pipelineLayout",
	"set"
})
public class VkDescriptorUpdateTemplateCreateInfo extends Structure {
	public static class ByValue extends VkDescriptorUpdateTemplateCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorUpdateTemplateCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DESCRIPTOR_UPDATE_TEMPLATE_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public int descriptorUpdateEntryCount;
	public VkDescriptorUpdateTemplateEntry.ByReference pDescriptorUpdateEntries;
	public int templateType;
	public long descriptorSetLayout;
	public int pipelineBindPoint;
	public long pipelineLayout;
	public int set;
}
