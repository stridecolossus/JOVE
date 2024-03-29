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
	"maxIndirectCommandsLayoutTokenCount",
	"maxObjectEntryCounts",
	"minSequenceCountBufferOffsetAlignment",
	"minSequenceIndexBufferOffsetAlignment",
	"minCommandsTokenBufferOffsetAlignment"
})
public class VkDeviceGeneratedCommandsLimitsNVX extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DEVICE_GENERATED_COMMANDS_LIMITS_NVX;
	public Pointer pNext;
	public int maxIndirectCommandsLayoutTokenCount;
	public int maxObjectEntryCounts;
	public int minSequenceCountBufferOffsetAlignment;
	public int minSequenceIndexBufferOffsetAlignment;
	public int minCommandsTokenBufferOffsetAlignment;
}
