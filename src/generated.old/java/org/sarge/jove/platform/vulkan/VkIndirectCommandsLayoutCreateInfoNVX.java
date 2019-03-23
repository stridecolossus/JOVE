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
	"pipelineBindPoint",
	"flags",
	"tokenCount",
	"pTokens"
})
public class VkIndirectCommandsLayoutCreateInfoNVX extends Structure {
	public static class ByValue extends VkIndirectCommandsLayoutCreateInfoNVX implements Structure.ByValue { }
	public static class ByReference extends VkIndirectCommandsLayoutCreateInfoNVX implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_INDIRECT_COMMANDS_LAYOUT_CREATE_INFO_NVX.value();
	public Pointer pNext;
	public int pipelineBindPoint;
	public int flags;
	public int tokenCount;
	public VkIndirectCommandsLayoutTokenNVX.ByReference pTokens;
}
