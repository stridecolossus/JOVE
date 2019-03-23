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
	"maxVariableDescriptorCount"
})
public class VkDescriptorSetVariableDescriptorCountLayoutSupportEXT extends Structure {
	public static class ByValue extends VkDescriptorSetVariableDescriptorCountLayoutSupportEXT implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorSetVariableDescriptorCountLayoutSupportEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_VARIABLE_DESCRIPTOR_COUNT_LAYOUT_SUPPORT_EXT.value();
	public Pointer pNext;
	public int maxVariableDescriptorCount;
}
