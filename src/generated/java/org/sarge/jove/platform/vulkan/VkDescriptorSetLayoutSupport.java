package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.VulkanBoolean;
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
	"supported"
})
public class VkDescriptorSetLayoutSupport extends VulkanStructure {
	public static class ByValue extends VkDescriptorSetLayoutSupport implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorSetLayoutSupport implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_SUPPORT;
	public Pointer pNext;
	public VulkanBoolean supported;
}
