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
	"maxPushDescriptors"
})
public class VkPhysicalDevicePushDescriptorPropertiesKHR extends Structure {
	public static class ByValue extends VkPhysicalDevicePushDescriptorPropertiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDevicePushDescriptorPropertiesKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_PUSH_DESCRIPTOR_PROPERTIES_KHR.value();
	public Pointer pNext;
	public int maxPushDescriptors;
}
