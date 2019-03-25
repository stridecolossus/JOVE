package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
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
	"deviceEvent"
})
public class VkDeviceEventInfoEXT extends VulkanStructure {
	public static class ByValue extends VkDeviceEventInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDeviceEventInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_EVENT_INFO_EXT;
	public Pointer pNext;
	public VkDeviceEventTypeEXT deviceEvent;
}