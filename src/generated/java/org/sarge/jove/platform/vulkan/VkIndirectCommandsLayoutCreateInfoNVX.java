package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
	"pipelineBindPoint",
	"flags",
	"tokenCount",
	"pTokens"
})
public class VkIndirectCommandsLayoutCreateInfoNVX extends VulkanStructure {
	public static class ByValue extends VkIndirectCommandsLayoutCreateInfoNVX implements Structure.ByValue { }
	public static class ByReference extends VkIndirectCommandsLayoutCreateInfoNVX implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.INDIRECT_COMMANDS_LAYOUT_CREATE_INFO_NVX;
	public Pointer pNext;
	public VkPipelineBindPoint pipelineBindPoint;
	public int flags;
	public int tokenCount;
	public Pointer pTokens;
}
