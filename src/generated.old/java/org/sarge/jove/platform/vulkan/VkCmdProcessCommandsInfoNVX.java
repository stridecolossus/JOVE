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
	"objectTable",
	"indirectCommandsLayout",
	"indirectCommandsTokenCount",
	"pIndirectCommandsTokens",
	"maxSequencesCount",
	"targetCommandBuffer",
	"sequencesCountBuffer",
	"sequencesCountOffset",
	"sequencesIndexBuffer",
	"sequencesIndexOffset"
})
public class VkCmdProcessCommandsInfoNVX extends Structure {
	public static class ByValue extends VkCmdProcessCommandsInfoNVX implements Structure.ByValue { }
	public static class ByReference extends VkCmdProcessCommandsInfoNVX implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_CMD_PROCESS_COMMANDS_INFO_NVX.value();
	public Pointer pNext;
	public long objectTable;
	public long indirectCommandsLayout;
	public int indirectCommandsTokenCount;
	public VkIndirectCommandsTokenNVX.ByReference pIndirectCommandsTokens;
	public int maxSequencesCount;
	public Pointer targetCommandBuffer;
	public long sequencesCountBuffer;
	public long sequencesCountOffset;
	public long sequencesIndexBuffer;
	public long sequencesIndexOffset;
}
