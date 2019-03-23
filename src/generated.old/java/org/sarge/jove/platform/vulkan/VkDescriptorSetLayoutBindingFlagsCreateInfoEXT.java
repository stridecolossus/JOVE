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
	"bindingCount",
	"pBindingFlags"
})
public class VkDescriptorSetLayoutBindingFlagsCreateInfoEXT extends Structure {
	public static class ByValue extends VkDescriptorSetLayoutBindingFlagsCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorSetLayoutBindingFlagsCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_BINDING_FLAGS_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public int bindingCount;
	public int pBindingFlags;
}
