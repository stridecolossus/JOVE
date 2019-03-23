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
	"dstSet",
	"dstBinding",
	"dstArrayElement",
	"descriptorCount",
	"descriptorType",
	"pImageInfo",
	"pBufferInfo",
	"pTexelBufferView"
})
public class VkWriteDescriptorSet extends Structure {
	public static class ByValue extends VkWriteDescriptorSet implements Structure.ByValue { }
	public static class ByReference extends VkWriteDescriptorSet implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET.value();
	public Pointer pNext;
	public long dstSet;
	public int dstBinding;
	public int dstArrayElement;
	public int descriptorCount;
	public int descriptorType;
	public VkDescriptorImageInfo.ByReference pImageInfo;
	public VkDescriptorBufferInfo.ByReference pBufferInfo;
	public long pTexelBufferView;
}
