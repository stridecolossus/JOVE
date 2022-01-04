package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
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
	"commandPool",
	"level",
	"commandBufferCount"
})
public class VkCommandBufferAllocateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.COMMAND_BUFFER_ALLOCATE_INFO;
	public Pointer pNext;
	public Handle commandPool;
	public VkCommandBufferLevel level;
	public int commandBufferCount;
}
