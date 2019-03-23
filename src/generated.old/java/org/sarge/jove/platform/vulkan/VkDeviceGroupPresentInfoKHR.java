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
	"swapchainCount",
	"pDeviceMasks",
	"mode"
})
public class VkDeviceGroupPresentInfoKHR extends Structure {
	public static class ByValue extends VkDeviceGroupPresentInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkDeviceGroupPresentInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_GROUP_PRESENT_INFO_KHR.value();
	public Pointer pNext;
	public int swapchainCount;
	public int pDeviceMasks;
	public int mode;
}
