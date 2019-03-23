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
	"viewportCount",
	"pViewports",
	"scissorCount",
	"pScissors"
})
public class VkPipelineViewportStateCreateInfo extends VulkanStructure {
	public static class ByValue extends VkPipelineViewportStateCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineViewportStateCreateInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public int viewportCount;
	public Pointer pViewports;
	public int scissorCount;
	public Pointer pScissors;
}
