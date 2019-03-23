package org.sarge.jove.platform.vulkan;

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
	"flags",
	"pInheritanceInfo"
})
public class VkCommandBufferBeginInfo extends Structure {
	public static class ByValue extends VkCommandBufferBeginInfo implements Structure.ByValue { }
	public static class ByReference extends VkCommandBufferBeginInfo implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO.value();
	public Pointer pNext;
	public int flags;
	public VkCommandBufferInheritanceInfo.ByReference pInheritanceInfo;
}
