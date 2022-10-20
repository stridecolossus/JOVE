package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
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
	public VkStructureType sType = VkStructureType.DESCRIPTOR_SET_LAYOUT_SUPPORT;
	public Pointer pNext;
	public boolean supported;
}
