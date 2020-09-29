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
	"size",
	"usage",
	"sharingMode",
	"queueFamilyIndexCount",
	"pQueueFamilyIndices"
})
public class VkBufferCreateInfo extends VulkanStructure {
	public static class ByValue extends VkBufferCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkBufferCreateInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public long size;
	public int usage;
	public VkSharingMode sharingMode;
	public int queueFamilyIndexCount;
	public Pointer pQueueFamilyIndices;
}
