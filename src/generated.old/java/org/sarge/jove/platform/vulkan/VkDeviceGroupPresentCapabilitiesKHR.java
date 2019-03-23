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
	"presentMask",
	"modes"
})
public class VkDeviceGroupPresentCapabilitiesKHR extends Structure {
	public static class ByValue extends VkDeviceGroupPresentCapabilitiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkDeviceGroupPresentCapabilitiesKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_GROUP_PRESENT_CAPABILITIES_KHR.value();
	public Pointer pNext;
	public final int[] presentMask = new int[32];
	public int modes;
}
