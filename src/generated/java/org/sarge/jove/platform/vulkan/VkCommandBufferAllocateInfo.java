package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
public class VkCommandBufferAllocateInfo extends VulkanStructure {
	public static class ByValue extends VkCommandBufferAllocateInfo implements Structure.ByValue { }
	public static class ByReference extends VkCommandBufferAllocateInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.COMMAND_BUFFER_ALLOCATE_INFO;
	public Pointer pNext;
	public Handle commandPool;
	public VkCommandBufferLevel level;
	public int commandBufferCount;
}
