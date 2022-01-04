package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"objectTable",
	"indirectCommandsLayout",
	"maxSequencesCount"
})
public class VkCmdReserveSpaceForCommandsInfoNVX extends VulkanStructure {
	public static class ByValue extends VkCmdReserveSpaceForCommandsInfoNVX implements Structure.ByValue { }
	public static class ByReference extends VkCmdReserveSpaceForCommandsInfoNVX implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.CMD_RESERVE_SPACE_FOR_COMMANDS_INFO_NVX;
	public Pointer pNext;
	public long objectTable;
	public long indirectCommandsLayout;
	public int maxSequencesCount;
}
