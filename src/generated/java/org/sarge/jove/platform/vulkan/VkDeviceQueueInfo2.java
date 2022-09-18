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
	"queueFamilyIndex",
	"queueIndex"
})
public class VkDeviceQueueInfo2 extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DEVICE_QUEUE_INFO_2;
	public Pointer pNext;
	public int flags;
	public int queueFamilyIndex;
	public int queueIndex;
}
