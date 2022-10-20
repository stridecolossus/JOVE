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
	"conditionalRenderingEnable"
})
public class VkCommandBufferInheritanceConditionalRenderingInfoEXT extends VulkanStructure {
	public VkStructureType sType = VkStructureType.COMMAND_BUFFER_INHERITANCE_CONDITIONAL_RENDERING_INFO_EXT;
	public Pointer pNext;
	public boolean conditionalRenderingEnable;
}
