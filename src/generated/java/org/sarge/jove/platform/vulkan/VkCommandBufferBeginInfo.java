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
	"flags",
	"pInheritanceInfo"
})
public class VkCommandBufferBeginInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.COMMAND_BUFFER_BEGIN_INFO;
	public Pointer pNext;
	public int flags;
	public Pointer pInheritanceInfo;
}
