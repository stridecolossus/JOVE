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
	"indirectCommandsTokenCount",
	"pIndirectCommandsTokens",
	"maxSequencesCount",
	"targetCommandBuffer",
	"sequencesCountBuffer",
	"sequencesCountOffset",
	"sequencesIndexBuffer",
	"sequencesIndexOffset"
})
public class VkCmdProcessCommandsInfoNVX extends VulkanStructure {
	public static class ByValue extends VkCmdProcessCommandsInfoNVX implements Structure.ByValue { }
	public static class ByReference extends VkCmdProcessCommandsInfoNVX implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.CMD_PROCESS_COMMANDS_INFO_NVX;
	public Pointer pNext;
	public long objectTable;
	public long indirectCommandsLayout;
	public int indirectCommandsTokenCount;
	public Pointer pIndirectCommandsTokens;
	public int maxSequencesCount;
	public Pointer targetCommandBuffer;
	public Pointer sequencesCountBuffer;
	public long sequencesCountOffset;
	public Pointer sequencesIndexBuffer;
	public long sequencesIndexOffset;
}
