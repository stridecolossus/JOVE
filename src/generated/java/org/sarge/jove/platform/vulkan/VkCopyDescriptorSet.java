package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"srcSet",
	"srcBinding",
	"srcArrayElement",
	"dstSet",
	"dstBinding",
	"dstArrayElement",
	"descriptorCount"
})
public class VkCopyDescriptorSet extends VulkanStructure {
	public static class ByValue extends VkCopyDescriptorSet implements Structure.ByValue { }
	public static class ByReference extends VkCopyDescriptorSet implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.COPY_DESCRIPTOR_SET;
	public Pointer pNext;
	public Pointer srcSet;
	public int srcBinding;
	public int srcArrayElement;
	public Pointer dstSet;
	public int dstBinding;
	public int dstArrayElement;
	public int descriptorCount;
}
