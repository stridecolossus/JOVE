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
	"flags",
	"usage",
	"handleType"
})
public class VkPhysicalDeviceExternalBufferInfo extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceExternalBufferInfo implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceExternalBufferInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_EXTERNAL_BUFFER_INFO;
	public Pointer pNext;
	public int flags;
	public VkBufferUsage usage;
	public VkExternalMemoryHandleTypeFlag handleType;
}
