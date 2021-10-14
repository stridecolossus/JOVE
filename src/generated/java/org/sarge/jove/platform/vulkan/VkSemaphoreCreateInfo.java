package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags"
})
public class VkSemaphoreCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.SEMAPHORE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
}
