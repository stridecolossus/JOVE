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
	"waitSemaphoreCount",
	"pWaitSemaphoreDeviceIndices",
	"commandBufferCount",
	"pCommandBufferDeviceMasks",
	"signalSemaphoreCount",
	"pSignalSemaphoreDeviceIndices"
})
public class VkDeviceGroupSubmitInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DEVICE_GROUP_SUBMIT_INFO;
	public Pointer pNext;
	public int waitSemaphoreCount;
	public Pointer pWaitSemaphoreDeviceIndices;
	public int commandBufferCount;
	public Pointer pCommandBufferDeviceMasks;
	public int signalSemaphoreCount;
	public Pointer pSignalSemaphoreDeviceIndices;
}
