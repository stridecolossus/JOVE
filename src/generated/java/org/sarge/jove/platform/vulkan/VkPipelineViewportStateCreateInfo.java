package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.*;

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
public class VkPipelineViewportStateCreateInfo extends VulkanStructure implements ByReference {
	public VkStructureType sType = VkStructureType.PIPELINE_VIEWPORT_STATE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public int viewportCount;
	public VkViewport pViewports;
	public int scissorCount;
	public VkRect2D.ByReference pScissors;
}
