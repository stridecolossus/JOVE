package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

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
	"conditionalRenderingEnable"
})
public class VkCommandBufferInheritanceConditionalRenderingInfoEXT extends VulkanStructure {
	public static class ByValue extends VkCommandBufferInheritanceConditionalRenderingInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkCommandBufferInheritanceConditionalRenderingInfoEXT implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.COMMAND_BUFFER_INHERITANCE_CONDITIONAL_RENDERING_INFO_EXT;
	public Pointer pNext;
	public VulkanBoolean conditionalRenderingEnable;
}
