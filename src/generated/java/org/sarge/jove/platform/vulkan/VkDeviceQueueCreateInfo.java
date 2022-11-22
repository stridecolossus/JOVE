package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"queueFamilyIndex",
	"queueCount",
	"pQueuePriorities"
})
public class VkDeviceQueueCreateInfo extends VulkanStructure implements ByReference {
	public VkStructureType sType = VkStructureType.DEVICE_QUEUE_CREATE_INFO;
	public Pointer pNext;
	public BitMask<VkDeviceQueueCreateFlag> flags;
	public int queueFamilyIndex;
	public int queueCount;
	public Pointer pQueuePriorities;
}
