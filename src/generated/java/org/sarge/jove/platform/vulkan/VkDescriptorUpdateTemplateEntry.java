package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"dstBinding",
	"dstArrayElement",
	"descriptorCount",
	"descriptorType",
	"offset",
	"stride"
})
public class VkDescriptorUpdateTemplateEntry extends VulkanStructure {
	public static class ByValue extends VkDescriptorUpdateTemplateEntry implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorUpdateTemplateEntry implements Structure.ByReference { }
	
	public int dstBinding;
	public int dstArrayElement;
	public int descriptorCount;
	public VkDescriptorType descriptorType;
	public long offset;
	public long stride;
}
