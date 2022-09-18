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
	"swapchain",
	"timeout",
	"semaphore",
	"fence",
	"deviceMask"
})
public class VkAcquireNextImageInfoKHR extends VulkanStructure {
	public VkStructureType sType = VkStructureType.ACQUIRE_NEXT_IMAGE_INFO_KHR;
	public Pointer pNext;
	public long swapchain;
	public long timeout;
	public Pointer semaphore;
	public Pointer fence;
	public int deviceMask;
}
