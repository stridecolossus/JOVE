package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
	"deviceMask"
})
public class VkDeviceGroupCommandBufferBeginInfo extends VulkanStructure {
	public static class ByValue extends VkDeviceGroupCommandBufferBeginInfo implements Structure.ByValue { }
	public static class ByReference extends VkDeviceGroupCommandBufferBeginInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.DEVICE_GROUP_COMMAND_BUFFER_BEGIN_INFO;
	public Pointer pNext;
	public int deviceMask;
}
