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
	"pipelineBindPoint",
	"flags",
	"tokenCount",
	"pTokens"
})
public class VkIndirectCommandsLayoutCreateInfoNVX extends VulkanStructure {
	public VkStructureType sType = VkStructureType.INDIRECT_COMMANDS_LAYOUT_CREATE_INFO_NVX;
	public Pointer pNext;
	public VkPipelineBindPoint pipelineBindPoint;
	public int flags;
	public int tokenCount;
	public Pointer pTokens;
}
