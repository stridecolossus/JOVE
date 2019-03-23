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
	"srcSet",
	"srcBinding",
	"srcArrayElement",
	"dstSet",
	"dstBinding",
	"dstArrayElement",
	"descriptorCount"
})
public class VkCopyDescriptorSet extends Structure {
	public static class ByValue extends VkCopyDescriptorSet implements Structure.ByValue { }
	public static class ByReference extends VkCopyDescriptorSet implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_COPY_DESCRIPTOR_SET.value();
	public Pointer pNext;
	public long srcSet;
	public int srcBinding;
	public int srcArrayElement;
	public long dstSet;
	public int dstBinding;
	public int dstArrayElement;
	public int descriptorCount;
}
