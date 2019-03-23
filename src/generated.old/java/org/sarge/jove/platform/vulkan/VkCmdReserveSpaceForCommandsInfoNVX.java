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
	"maxSequencesCount"
})
public class VkCmdReserveSpaceForCommandsInfoNVX extends Structure {
	public static class ByValue extends VkCmdReserveSpaceForCommandsInfoNVX implements Structure.ByValue { }
	public static class ByReference extends VkCmdReserveSpaceForCommandsInfoNVX implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_CMD_RESERVE_SPACE_FOR_COMMANDS_INFO_NVX.value();
	public Pointer pNext;
	public long objectTable;
	public long indirectCommandsLayout;
	public int maxSequencesCount;
}
