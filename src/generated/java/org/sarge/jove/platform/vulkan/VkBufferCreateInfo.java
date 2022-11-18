package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitField;

import com.sun.jna.Pointer;
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
	public VkStructureType sType = VkStructureType.BUFFER_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public long size;
	public BitField<VkBufferUsageFlag> usage;
	public VkSharingMode sharingMode;
	public int queueFamilyIndexCount;
	public Pointer pQueueFamilyIndices;
}
