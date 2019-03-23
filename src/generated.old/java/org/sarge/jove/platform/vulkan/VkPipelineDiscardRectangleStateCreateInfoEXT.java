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
	"discardRectangleMode",
	"discardRectangleCount",
	"pDiscardRectangles"
})
public class VkPipelineDiscardRectangleStateCreateInfoEXT extends Structure {
	public static class ByValue extends VkPipelineDiscardRectangleStateCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPipelineDiscardRectangleStateCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_DISCARD_RECTANGLE_STATE_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public int flags;
	public int discardRectangleMode;
	public int discardRectangleCount;
	public VkRect2D.ByReference pDiscardRectangles;
}
