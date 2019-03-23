package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

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
public class VkDeviceGroupSubmitInfo extends Structure {
	public static class ByValue extends VkDeviceGroupSubmitInfo implements Structure.ByValue { }
	public static class ByReference extends VkDeviceGroupSubmitInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_GROUP_SUBMIT_INFO.value();
	public Pointer pNext;
	public int waitSemaphoreCount;
	public int pWaitSemaphoreDeviceIndices;
	public int commandBufferCount;
	public int pCommandBufferDeviceMasks;
	public int signalSemaphoreCount;
	public int pSignalSemaphoreDeviceIndices;
}
