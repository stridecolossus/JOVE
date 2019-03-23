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
	"commandPool",
	"level",
	"commandBufferCount"
})
public class VkCommandBufferAllocateInfo extends Structure {
	public static class ByValue extends VkCommandBufferAllocateInfo implements Structure.ByValue { }
	public static class ByReference extends VkCommandBufferAllocateInfo implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO.value();
	public Pointer pNext;
	public Pointer commandPool;
	public int level;
	public int commandBufferCount;
}
