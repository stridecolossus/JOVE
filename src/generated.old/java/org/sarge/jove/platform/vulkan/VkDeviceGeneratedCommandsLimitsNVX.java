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
	"maxIndirectCommandsLayoutTokenCount",
	"maxObjectEntryCounts",
	"minSequenceCountBufferOffsetAlignment",
	"minSequenceIndexBufferOffsetAlignment",
	"minCommandsTokenBufferOffsetAlignment"
})
public class VkDeviceGeneratedCommandsLimitsNVX extends Structure {
	public static class ByValue extends VkDeviceGeneratedCommandsLimitsNVX implements Structure.ByValue { }
	public static class ByReference extends VkDeviceGeneratedCommandsLimitsNVX implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_GENERATED_COMMANDS_LIMITS_NVX.value();
	public Pointer pNext;
	public int maxIndirectCommandsLayoutTokenCount;
	public int maxObjectEntryCounts;
	public int minSequenceCountBufferOffsetAlignment;
	public int minSequenceIndexBufferOffsetAlignment;
	public int minCommandsTokenBufferOffsetAlignment;
}
