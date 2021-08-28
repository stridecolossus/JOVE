package org.sarge.jove.platform.vulkan;

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
	"flags",
	"queueFamilyIndex",
	"queueIndex"
})
public class VkDeviceQueueInfo2 extends VulkanStructure {
	public static class ByValue extends VkDeviceQueueInfo2 implements Structure.ByValue { }
	public static class ByReference extends VkDeviceQueueInfo2 implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.DEVICE_QUEUE_INFO_2;
	public Pointer pNext;
	public int flags;
	public int queueFamilyIndex;
	public int queueIndex;
}
