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
	"conditionalRenderingEnable"
})
public class VkCommandBufferInheritanceConditionalRenderingInfoEXT extends Structure {
	public static class ByValue extends VkCommandBufferInheritanceConditionalRenderingInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkCommandBufferInheritanceConditionalRenderingInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_CONDITIONAL_RENDERING_INFO_EXT.value();
	public Pointer pNext;
	public boolean conditionalRenderingEnable;
}
