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
	"dstSet",
	"dstBinding",
	"dstArrayElement",
	"descriptorCount",
	"descriptorType",
	"pImageInfo",
	"pBufferInfo",
	"pTexelBufferView"
})
public class VkWriteDescriptorSet extends VulkanStructure {
	public static class ByValue extends VkWriteDescriptorSet implements Structure.ByValue { }
	public static class ByReference extends VkWriteDescriptorSet implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
	public Pointer pNext;
	public Pointer dstSet;
	public int dstBinding;
	public int dstArrayElement;
	public int descriptorCount;
	public VkDescriptorType descriptorType;
	public VkDescriptorImageInfo pImageInfo;
	public VkDescriptorBufferInfo pBufferInfo;
	public Pointer pTexelBufferView; // TODO
}
