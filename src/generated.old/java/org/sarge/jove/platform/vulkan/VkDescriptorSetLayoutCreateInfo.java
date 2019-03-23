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
	"bindingCount",
	"pBindings"
})
public class VkDescriptorSetLayoutCreateInfo extends Structure {
	public static class ByValue extends VkDescriptorSetLayoutCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorSetLayoutCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public int bindingCount;
	public VkDescriptorSetLayoutBinding.ByReference pBindings;
}
