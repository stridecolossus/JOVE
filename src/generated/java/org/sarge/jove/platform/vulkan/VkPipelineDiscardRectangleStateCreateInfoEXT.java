package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
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
	"flags",
	"discardRectangleMode",
	"discardRectangleCount",
	"pDiscardRectangles"
})
public class VkPipelineDiscardRectangleStateCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkPipelineDiscardRectangleStateCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPipelineDiscardRectangleStateCreateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_DISCARD_RECTANGLE_STATE_CREATE_INFO_EXT;
	public Pointer pNext;
	public int flags;
	public VkDiscardRectangleModeEXT discardRectangleMode;
	public int discardRectangleCount;
	public Pointer pDiscardRectangles;
}
